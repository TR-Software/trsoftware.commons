package solutions.trsoftware.commons.server.util.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Implements {@link JsonSerializer} using the GSON library.
 *
 * @author Alex, 12/31/2014
 */
public class JsonSerializerImpl<T> implements JsonSerializer<T> {

  protected final Gson gson;

  protected final Class<T> valueType;

  /**
   * @param valueType Needs to be passed explicitly despite this class already having a generic type argument,
   * because Java's generic "type erasure" (the removal of actual type arguments for instances at compile time)
   * makes it impossible to derive the actual generic type from an instance.
   * Read <a href="http://www.javacodegeeks.com/2013/12/advanced-java-generics-retreiving-generic-type-arguments.html">
   * this article</a> for more info.
   */
  public JsonSerializerImpl(Class<T> valueType) {
    this.valueType = valueType;
    GsonBuilder gsonBuilder = new GsonBuilder();
    configureGson(gsonBuilder);
    gson = gsonBuilder.create();
  }

  /**
   * Sublcasses can override this method to change the GSON output/parsing settings.
   * See https://sites.google.com/site/gson/gson-user-guide
   */
  protected void configureGson(GsonBuilder gsonBuilder) {
    gsonBuilder.setDateFormat("MMM d, yyyy h:mm:ss.S a");  // preserve millisecond precision in dates
  }

  @Override
  public T parseJson(String json) {
    return (T)gson.fromJson(json, getValueType());
  }

  @Override
  public String toJson(T instance) {
    return gson.toJson(instance, getValueType());
  }

  @Override
  public Class<T> getValueType() {
    return valueType;
  }

}
