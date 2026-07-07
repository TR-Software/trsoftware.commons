package solutions.trsoftware.commons.shared.util;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Alex
 * @since 6/6/2026
 */
public class ListBuilder<L extends List<E>, E> {

  // TODO(6/6/2026): experimental version of MapDecorator for lists

  private final L list;


  public ListBuilder(Supplier<L> factory) {
    list = factory.get();
  }

  public ListBuilder<L, E> add(E e) {
    list.add(e);
    return this;
  }

  public ListBuilder<L, E> addAll(@Nonnull Collection<? extends E> c) {
    list.addAll(c);
    return this;
  }

  public ListBuilder<L, E> remove(E o) {
    list.remove(o);
    return this;
  }

  public ListBuilder<L, E> removeAll(@Nonnull Collection<? extends E> c) {
    list.removeAll(c);
    return this;
  }

  public L getList() {
    return list;
  }


  // Factory methods:

  public static <L extends List<E>, E> ListBuilder<L, E> listBuilder(@Nonnull Supplier<L> listSupplier) {
    return new ListBuilder<>(listSupplier);
  }

  public static <E> ListBuilder<ArrayList<E>, E> listBuilder() {
    return listBuilder(ArrayList::new);
  }

  /**
   * @return builder for a new {@link ArrayList} initially containing the elements from the given collection
   */
  public static <E> ListBuilder<ArrayList<E>, E> copyOf(Collection<? extends E> c) {
    return listBuilder(() -> new ArrayList<>(c));
  }

}
