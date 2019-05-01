package solutions.trsoftware.commons.server.util.persistence;

import com.google.gson.GsonBuilder;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Maps an object to a JSON file (which contains a jsonized representation of the object)
 *
 * @author Alex
 * @since 4/23/2019
 */
public class JsonFileDAO<T> implements PersistentObjectDAO<T> {

  private Path file;
  private GsonSerializer<T> jsonSerializer;

  public JsonFileDAO(Path file, Class<? extends T> targetObjectType) {
    this.file = Objects.requireNonNull(file);
    // TODO: cont here: assert that the given class can be jsonized (i.e. it has fields); perhaps use com.google.gson.Gson.getAdapter(com.google.gson.reflect.TypeToken<T>) to check this?
    jsonSerializer = new GsonSerializer<T>(targetObjectType) {
      @Override
      protected void configureGson(GsonBuilder gsonBuilder) {
        super.configureGson(gsonBuilder);
        gsonBuilder.setPrettyPrinting();
      }
    };
  }

  public JsonFileDAO(File file, Class<T> targetObjectType) {
    this(file.toPath(), targetObjectType);
  }



  @Override
  public synchronized void persist(T entity) throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(file)) {
      jsonSerializer.toJson(entity, writer);
    }
  }

  @Nullable
  @Override
  public synchronized T load() throws IOException {
    if (Files.isRegularFile(file)) {
      try (BufferedReader reader = Files.newBufferedReader(file)) {
        return jsonSerializer.parseJson(reader);
      }
    }
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    JsonFileDAO<?> that = (JsonFileDAO<?>)o;

    return file.equals(that.file);
  }

  @Override
  public int hashCode() {
    return file.hashCode();
  }
}
