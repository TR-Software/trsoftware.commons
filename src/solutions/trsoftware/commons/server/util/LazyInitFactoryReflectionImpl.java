package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.client.util.LazyInitFactory;

/**
 * Mar 10, 2011
 *
 * @author Alex
 */
public class LazyInitFactoryReflectionImpl<T> extends LazyInitFactory<T> {

  private final Class<T> type;

  public LazyInitFactoryReflectionImpl(final Class<T> type) {
    this.type = type;
  }

  @Override
  protected T create() {
    try {
      return type.newInstance();
    }
    catch (InstantiationException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    catch (IllegalAccessException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
