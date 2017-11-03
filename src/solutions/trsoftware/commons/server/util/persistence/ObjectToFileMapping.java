package solutions.trsoftware.commons.server.util.persistence;

import com.google.gson.GsonBuilder;
import solutions.trsoftware.commons.client.util.MapUtils;
import solutions.trsoftware.commons.client.util.callables.Function2;
import solutions.trsoftware.commons.server.io.ServerIOUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating a proxy object that intercepts method invocations on an object that you want serialized
 * to disk after each setter method invocation.
 *
 * Each {@link #target} object must be persisted in a separate file.
 *
 * Usage: Call one of the static factory methods (e.g. {@link #createProxyFromFile(File, Class, Class)}) to create a proxy.
 *
 * @author Alex, 8/28/2015
 */
public class ObjectToFileMapping implements InvocationHandler {

  // NOTE: this class can be generalized to implement persistence using a back end other than a file and/or a format other than JSON.


  /**
   * This flyweight ensures a 1-1 mapping between objects and files (prevents the race condition of two different instances
   * being persisted to the same file).  The values in this map hold the {@link #target} object mapped to the file
   * specified by the corresponding key.
   */
  private static Map<File, ObjectToFileMapping> flyweight = new ConcurrentHashMap<File, ObjectToFileMapping>();


  /** The file holding the serialized representation of the object behind this proxy  */
  private final File file;

  /**
   * The object behind this proxy, which will be serialized to {@link #file} after every setter method invocation.
   * In other words, this is the object to be read/written to/from disk.
   */
  private Object target;

  private JsonSerializer jsonSerializer;

  private static final Function2<File, Object, ObjectToFileMapping> factoryFromTargetInstance = new Function2<File, Object, ObjectToFileMapping>() {
    @Override
    public ObjectToFileMapping call(File file, Object defaultTarget) {
      return new ObjectToFileMapping(file, defaultTarget.getClass(), defaultTarget);
    }
  };

  private static final Function2<File, Class, ObjectToFileMapping> factoryFromTargetType = new Function2<File, Class, ObjectToFileMapping>() {
    @Override
    public ObjectToFileMapping call(File file, Class targetObjectType) {
      return new ObjectToFileMapping(file, targetObjectType, null);
    }
  };

  /**
   * Creates the {@link #target} object based on the contents of the given file or uses defaultTarget if the file doesn't exist.
   * This constructor is private to ensure that all instances are registered with the {@link #flyweight}.  Use one
   * of the static factory methods instead.
   *
   * @param file where the serialized form of the {@link #target} object is persisted.  Each {@link #target} object
   * must be persisted in a separate file.
   * @param targetObjectType the {@link #target} object's class.
   * @param defaultTarget A default instance to use for {@link #target} if the file doesn't exist.
   */
  private ObjectToFileMapping(File file, Class targetObjectType, Object defaultTarget) {
    this.file = file;
    jsonSerializer = new JsonSerializerImpl(targetObjectType) {
      @Override
      protected void configureGson(GsonBuilder gsonBuilder) {
        super.configureGson(gsonBuilder);
        gsonBuilder.setPrettyPrinting();
      }
    };
    // if the file exists, use it instead of the passed defaultTarget param (for server restart recovery)
    if (file.exists()) {
      try {
        target = jsonSerializer.parseJson(ServerIOUtils.readCharactersIntoString(ServerIOUtils.readFileUTF8(this.file)).trim());
      }
      catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
    else {
      assert defaultTarget != null;
      target = defaultTarget;
    }
  }

  /**
   * Factory method that creates the proxy defined by this instance.
   *
   * @return A proxy that will intercept invocations of methods defined in the given interfaces on the encapsulated
   * {@link #target} object.  The result can safely be cast to any of the given interfaces in order to be used
   * in lieu of the {@link #target} object in order for the interception to work.  The interception is implemented
   * by the {@link #invoke(Object, Method, Object[])} method.
   */
  private Proxy newProxyInstance(Class<?>... interfaces) {
    return (Proxy)Proxy.newProxyInstance(getClass().getClassLoader(), interfaces, this);
  }

  /**
   * Implements the proxy functionality, i.e. the interception of methods invoked on the {@link #target} object whose
   * name starts with "set" in order to write the {@link #target} object to disk after each such invocation.
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Object ret = method.invoke(target, args);
    if (method.getName().startsWith("set"))
      synchronized (this) {
        ServerIOUtils.writeStringToFileUTF8(file, jsonSerializer.toJson(target));
      }
    return ret;
  }

  /**
   * Creates the proxy defined by the given parameters, using the given default instance for {@link #target}
   * (object behind the proxy) iff the file doesn't already exist.
   *
   * @param file where the {@link #target} object is persisted.
   * @param defaultTarget A default instance to use for {@link #target} if the file doesn't exist. Either way, this
   * parameter cannot be null, because it's needed to determine the class of the object to be read/written to/from disk.
   * To determine whether this default was used as the proxy target or whether a new instance was created can
   * {@link #getProxyTarget(File)}  (e.g. <code>getProxyTarget(file) == defaultTarget</code>)
   * @param interfaces the returned proxy can be cast to any of the given interfaces (which must be implemented
   * by the {@link #target} object).
   *
   * @return A proxy that will intercept invocations of methods defined in the given interfaces on the encapsulated
   * {@link #target} object.  The result can safely be cast to any of the given interfaces in order to be used
   * in lieu of the {@link #target} object in order for the interception to work.  The interception is implemented
   * by the {@link #invoke(Object, Method, Object[])} method.
   */
  public static synchronized Proxy createProxy(File file, Object defaultTarget, Class<?>... interfaces) {
    return MapUtils.getOrInsert(flyweight, file, factoryFromTargetInstance, file, defaultTarget).newProxyInstance(interfaces);
  }

  /**
   * Creates the proxy defined by the given parameters, using the given default instance for {@link #target}
   * (object behind the proxy) iff the file doesn't already exist.
   *
   * @param file where the {@link #target} object is persisted.
   * @param defaultTarget A default instance to use for {@link #target} if the file doesn't exist. Either way, this
   * parameter cannot be null, because it's needed to determine the class of the object to be read/written to/from disk.
   * To determine whether this default was used as the proxy target or whether a new instance was created can
   * {@link #getProxyTarget(File)}  (e.g. <code>getProxyTarget(file) == defaultTarget</code>)
   * @param proxiedInterface the returned proxy will be cast to this interface (which must be implemented by the {@link #target} object).
   *
   * @return A proxy that will intercept invocations of methods defined in the given interfaces on the encapsulated
   * {@link #target} object.  The result can safely be cast to any of the given interfaces in order to be used
   * in lieu of the {@link #target} object in order for the interception to work.  The interception is implemented
   * by the {@link #invoke(Object, Method, Object[])} method.
   */
  public static synchronized <I, O extends I> I createProxy(File file, O defaultTarget, Class<I> proxiedInterface) {
    return (I)createProxy(file, defaultTarget, new Class[]{proxiedInterface});
  }

  /**
   * Creates the proxy defined by the given parameters.
   * The {@link #target} (object behind the proxy) will be instantiated from on the contents of the given file.
   *
   * @param file where the {@link #target} object is persisted.
   * @param targetObjectType the class of the object to be read/written to/from disk.
   * @param proxiedInterfaces the returned proxy can be cast to any of the given interfaces (which must be implemented
   * by the {@link #target} object).
   *
   * @return A proxy that will intercept invocations of methods defined in the given interfaces on the encapsulated
   * {@link #target} object.  The result can safely be cast to any of the given interfaces in order to be used
   * in lieu of the {@link #target} object in order for the interception to work.  The interception is implemented
   * by the {@link #invoke(Object, Method, Object[])} method.
   */
  public static synchronized Proxy createProxyFromFile(File file, Class targetObjectType, Class... proxiedInterfaces) {
    return MapUtils.getOrInsert(flyweight, file, factoryFromTargetType, file, targetObjectType).newProxyInstance(proxiedInterfaces);
  }


  /**
   * Creates the proxy defined by the given parameters.
   * The {@link #target} (object behind the proxy) will be instantiated from on the contents of the given file.
   *
   * @param file where the {@link #target} object is persisted.
   * @param targetObjectType the class of the object to be read/written to/from disk.
   * @param proxiedInterface the returned proxy will be cast to this interface (which must be implemented by the {@link #target} object).
   *
   * @return A proxy that will intercept invocations of methods defined in the given interfaces on the encapsulated
   * {@link #target} object.  The result can safely be cast to any of the given interfaces in order to be used
   * in lieu of the {@link #target} object in order for the interception to work.  The interception is implemented
   * by the {@link #invoke(Object, Method, Object[])} method.
   */
  public static synchronized <I, O extends I> I createProxyFromFile(File file, Class<O> targetObjectType, Class<I> proxiedInterface) {
    return (I)createProxyFromFile(file, targetObjectType, new Class[]{proxiedInterface});
  }

  /**
   * @return the object mapped to the given file, or null if no mapping has been created.
   */
  public static Object getProxyTarget(File file) {
    ObjectToFileMapping builder = flyweight.get(file);
    return builder.target;
  }

}
