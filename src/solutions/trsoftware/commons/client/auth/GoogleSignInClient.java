package solutions.trsoftware.commons.client.auth;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Element;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import solutions.trsoftware.commons.client.debug.Debug;
import solutions.trsoftware.commons.client.event.Events;
import solutions.trsoftware.commons.shared.util.CollectionUtils;
import solutions.trsoftware.commons.shared.util.compare.RichComparable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

/**
 * Wrapper for the Google Sign In JavaScript API client.
 * <p>
 * The singleton instance of this class is configured by the first caller of {@link #getInstance(IdConfiguration, Callback)}.
 * <p>
 * Fires {@link GoogleSignInEvent} when user completes the Google account authentication precess
 * (via the One Tap prompt or popup-window); see {@link #addSignInHandler(GoogleSignInEvent.Handler)}
 *
 * @see GoogleSignInButton
 * @see <a href="https://developers.google.com/identity/gsi/web/reference/js-reference">
 *   Sign In With Google JavaScript API reference</a>
 * @author Alex
 * @since 1/3/2024
 */
public class GoogleSignInClient {
  public static final String SCRIPT_URL = "https://accounts.google.com/gsi/client";

  private static final Logger LOGGER = Logger.getLogger(GoogleSignInClient.class.getName());

  // TODO: maybe extract all the ScriptInjector logic to a util class (can share impl details with the ReCaptcha script)

  public enum ScriptState implements RichComparable<ScriptState> {
    /**
     * The script hasn't been injected yet, and is not in the process of loading.
     */
    NOT_INJECTED,
    /**
     * The script has been injected and now waiting for it to finish loading.
     */
    INJECTING,
    /**
     * The script has successfully loaded and is available to use.
     * This state is the result of {@link ScriptInjector} invoking {@link Callback#onSuccess}
     */
    LOADED,
    /**
     * The script failed to load and is not available to use.
     * This state is the result of {@link ScriptInjector} invoking {@link Callback#onFailure},
     * or {@link #initialize(IdConfiguration)} throwing an exception (which happens if the browser
     * doesn't support the new JS language features used by the script, e.g. when running legacy DevMode)
     */
    FAILED;
  }

  private static ScriptState scriptState = ScriptState.NOT_INJECTED;

  private static GoogleSignInClient instance;

  private static void injectScript(Callback<Void, Exception> callback) {
    Preconditions.checkState(scriptState == ScriptState.NOT_INJECTED,
        "The %s script has already been injected (current state: %s)", SCRIPT_URL, scriptState);
    ScriptInjector.fromUrl(SCRIPT_URL)
        .setWindow(ScriptInjector.TOP_WINDOW)
        .setCallback(callback)
        .inject();
    scriptState = ScriptState.INJECTING;
  }

  public static ScriptState getScriptState() {
    return scriptState;
  }

  /**
   * Returns the global {@link GoogleSignInClient} instance that was previously created by the first invocation
   * of {@link #getInstance(IdConfiguration, Callback)}.
   * <p>
   * <strong>Warning:</strong> this method should only be used if can be sure that the
   * {@link #getInstance(IdConfiguration, Callback)} has already been invoked with the desired {@link IdConfiguration}
   * and the script loading was successful.
   *
   * @throws IllegalStateException if {@link #getInstance(IdConfiguration, Callback)} hasn't been invoked yet
   *   or the script hasn't finished loading.
   */
  public static GoogleSignInClient getInstance() {
    Preconditions.checkState(instance != null,
        "%s instance not available yet; did you forget to call getInstance(IdConfiguration, Callback)?",
        GoogleSignInClient.class.getSimpleName());
    return instance;
  }


  /**
   * Returns the singleton {@link GoogleSignInClient} instance via the provided callback after the GSI library script
   * has finished loading.  If the script has already been loaded and intialized, the callback will be invoked immediately.
   * <p>
   * The first caller of this method gets to set the global {@link IdConfiguration} for the client, and any subsequent
   * invocations with a different {@link IdConfiguration} will throw an {@link IllegalStateException}, so callers
   * should be aware of this potential race condition. This constraint is necessitated by the GSI library design,
   * which states that:
   * <blockquote cite="https://developers.google.com/identity/gsi/web/reference/js-reference#google.accounts.id.initialize">
   *   <em>The {@code google.accounts.id.initialize} method should be called only once, even if you use both One Tap and button in the same web page.</em>
   * </blockquote>
   *
   * @param config the global configuration, containing the application's client ID and other global parameters for the GSI client.
   * @param callback will receive the {@link GoogleSignInClient} instance when it becomes available, or a
   *   {@linkplain Callback#onFailure(Object) failure notification} if the script loading is unsuccessful,
   *   or {@link #initialize(IdConfiguration)} threw an exception (see {@link ScriptState#FAILED})
   * @throws IllegalArgumentException if this method has already been invoked with a different {@link IdConfiguration}
   */
  public static void getInstance(@Nonnull IdConfiguration config, Callback<GoogleSignInClient, Exception> callback) {
    requireNonNull(config, "config");
    if (globalIdConfiguration == null) {
      assert scriptState == ScriptState.NOT_INJECTED;
      // the first caller gets to dictate the global idConfig (acceptable race condition)
      globalIdConfiguration = config;
    }
    else if (!(Debug.ENABLED || globalIdConfiguration.isEqualTo(config))) {
      // fail early
      throw new IllegalStateException(Strings.lenientFormat(
          "%s already configured with a different %s (%s)",
          GoogleSignInClient.class.getSimpleName(), IdConfiguration.class.getSimpleName(), globalIdConfiguration.toJSON()
      ));
    }

    switch (scriptState) {
      case NOT_INJECTED: {
        addInstanceCallback(callback);
        injectScript(new Callback<Void, Exception>() {
          @Override
          public void onSuccess(Void result) {
            assert instance == null;
            try {
              // client.initialize could throw an exception (e.g. if in DevMode on Chrome 35)
              instance = new GoogleSignInClient(config);
              scriptState = ScriptState.LOADED;
              notifyScriptLoaded(instance);
            }
            catch (Exception exception) {
              scriptState = ScriptState.FAILED;
              notifyScriptFailed(exception);
            }
          }
          @Override
          public void onFailure(Exception reason) {
            scriptState = ScriptState.FAILED;
            LOGGER.log(Level.SEVERE, "Error injecting '" + SCRIPT_URL + "' using ScriptInjector", reason);
            notifyScriptFailed(reason);
            // TODO: should future calls retry loading the script?
          }
        });
      }
        break;
      case INJECTING:
        addInstanceCallback(callback);
        break;
      case LOADED:
        assert instance != null;
        callback.onSuccess(instance);  // delegate to the other getInstance method, which ensures that idConfig matches
        break;
      case FAILED:
        assert scriptException != null;
        callback.onFailure(scriptException);
        break;
    }
  }

  private static IdConfiguration globalIdConfiguration;

  /**
   * The exception returned by the {@link ScriptInjector} callback (which indicates the script couldn't be downloaded)
   * or thrown by {@link #initialize(IdConfiguration)} (which indicates that the client library isn't working properly).
   * @see ScriptState#FAILED
   */
  private static Exception scriptException;

  /**
   * Callbacks waiting for the script to finish loading.
   * @see #getInstance(IdConfiguration, Callback)
   */
  private static List<Callback<GoogleSignInClient, Exception>> instanceCallbacks;

  private static void addInstanceCallback(Callback<GoogleSignInClient, Exception> callback) {
    assert instance == null;
    if (instanceCallbacks == null)
      instanceCallbacks = new ArrayList<>();
    instanceCallbacks.add(callback);
  }

  /**
   * Invokes {@link Callback#onSuccess(Object)} for all pending {@link #instanceCallbacks}
   * @param instance the argument for {@link Callback#onSuccess(Object)}
   */
  private static void notifyScriptLoaded(GoogleSignInClient instance) {
    notifyInstanceCallbacks(callback -> callback.onSuccess(instance));
  }

  /**
   * Invokes {@link Callback#onFailure(Object)} for all pending {@link #instanceCallbacks}
   */
  private static void notifyScriptFailed(Exception exception) {
    reportScriptException(exception);
    notifyInstanceCallbacks(callback -> callback.onFailure(exception));
  }

  /**
   * Invokes {@link UncaughtExceptionHandler#onUncaughtException(Throwable)} to report the given exception
   * and assigns it to {@link #scriptException}.  This will happen only once per lifetime of the application.
   *
   * @param exception exception returned by {@link ScriptInjector} or thrown by {@link #initialize(IdConfiguration)}
   */
  private static void reportScriptException(Exception exception) {
    if (scriptException == null) {
      // not reported yet
      scriptException = exception;
      GWT.getUncaughtExceptionHandler().onUncaughtException(exception);
    }
  }

  private static void notifyInstanceCallbacks(Consumer<Callback<GoogleSignInClient, Exception>> callbackConsumer) {
    if (instanceCallbacks != null) {
      CollectionUtils.tryForEach(instanceCallbacks, callbackConsumer);
      instanceCallbacks = null;
    }
  }


  // Instance fields / methods:

  private final IdConfiguration config;
  private final EventBus eventBus = Events.BUS;

  /**
   * Initializes the native GSI client using the given config.
   * @throws JavaScriptException if the call to {@code google.accounts.id.initialize} fails,
   *   which could happen if the browser doesn't support the new JS language features used by the script
   *   (e.g. when running legacy DevMode on Chrome 35)
   * @see <a href="https://developers.google.com/identity/gsi/web/reference/js-reference#google.accounts.id.initialize">
   *   google.accounts.id.initialize</a>
   */
  private GoogleSignInClient(IdConfiguration config) {
    this.config = config;
    initialize(config);
  }

  public IdConfiguration getConfig() {
    return config;
  }

  private void initialize(IdConfiguration config) {
    config.setCallback(response -> eventBus.fireEvent(new GoogleSignInEvent(response)));
    initializeNative(config);
  }

  private native void initializeNative(IdConfiguration config) /*-{
    $wnd.google.accounts.id.initialize(config);
  }-*/;

  /**
   * Renders a GSI button inside the given element.
   *
   * @param parent the parent container
   * @param options button options
   * @see <a href="https://developers.google.com/identity/gsi/web/reference/js-reference#google.accounts.id.renderButton">
   *   <code>google.accounts.id.renderButton</code></a>
   */
  public native void renderButton(Element parent, GsiButtonConfiguration options) /*-{
    $wnd.google.accounts.id.renderButton(parent, options);
  }-*/;

  public HandlerRegistration addSignInHandler(GoogleSignInEvent.Handler handler) {
    return eventBus.addHandler(GoogleSignInEvent.TYPE, handler);
  }
}
