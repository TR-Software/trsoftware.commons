package solutions.trsoftware.commons.server.util.persistence;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Handles the persistence of an instance of type {@link T}.
 * <p>
 * An instance of this DAO has a 1-1 correspondence with an instance being persisted, thereby avoiding the need to
 * pass some kind of lookup key in order to retrieve the object.  For example, if the underlying storage mechanism
 * is a file, then that file always stores the same instance.
 * <p>
 * <strong>NOTE:</strong> implementing classes must define {@link #equals(Object)} and {@link #hashCode()}
 *
 * @param <T> type of the objects being persisted
 *
 * @author Alex
 * @since 4/23/2019
 */
public interface PersistentObjectDAO<T> {

  /**
   * Persists the given object to the underlying data store.
   *
   * @param entity the object to persist
   */
  void persist(T entity) throws IOException;


  /**
   * Loads the instance from the underlying data store.
   *
   * @return an instance of type {@link T} loaded from from the underlying data store, or {@code null} if not found
   */
  @Nullable T load() throws IOException;

}
