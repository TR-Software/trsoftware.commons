package solutions.trsoftware.commons.server.util.gql;

import com.google.common.annotations.Beta;
import solutions.trsoftware.commons.shared.util.ListUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a GQL query, which follows the given grammar:
 * <blockquote cite="https://cloud.google.com/appengine/docs/standard/python/datastore/gqlreference"><pre>{@code
 * SELECT [DISTINCT] [* | <property list> | __key__]
 *   [FROM <kind>]
 *   [WHERE <condition> [AND <condition> ...]]
 *   [ORDER BY <property> [ASC | DESC] [, <property> [ASC | DESC] ...]]
 *   [LIMIT [<offset>,]<count>]
 *   [OFFSET <offset>]
 *
 *   <property list> := <property> [, <property> ...]
 *   <condition> := <property> {< | <= | > | >= | = | != } <value>
 *   <condition> := <property> IN <list>
 *   <condition> := ANCESTOR IS <entity or key>
 *   <list> := (<value> [, <value> ...]])
 * }</pre></blockquote>
 *
 * @see <a href="https://cloud.google.com/appengine/docs/standard/python/datastore/gqlreference">GQL Reference</a>
 * @author Alex
 * @since 1/6/2020
 */
@Beta  // Not tested
public class GqlQuery implements GqlElement {

  // TODO: unit test this class (and the nested builder)

  private SelectClause selectClause;
  private FromClause fromClause;
  private WhereClause whereClause;
  private OrderByClause orderByClause;
  private LimitClause limitClause;
  private OffsetClause offsetClause;

  /**
   * Not directly instantiable; use the {@linkplain Builder builder} returned by one of the static "select" methods or
   * {@link #newBuilder()}.
   *
   * @see #selectStar()
   * @see #selectKey()
   * @see #select(List)
   * @see #selectDistinct(List)
   */
  private GqlQuery() {

  }

  public SelectClause getSelectClause() {
    return selectClause;
  }

  public FromClause getFromClause() {
    return fromClause;
  }

  public WhereClause getWhereClause() {
    return whereClause;
  }

  public OrderByClause getOrderByClause() {
    return orderByClause;
  }

  public LimitClause getLimitClause() {
    return limitClause;
  }

  public OffsetClause getOffsetClause() {
    return offsetClause;
  }

  @Override
  public String toGql() {
    return Stream.of(selectClause, fromClause, whereClause, orderByClause, limitClause, offsetClause)
        .filter(Objects::nonNull).map(GqlElement::toGql).collect(Collectors.joining(" "));
  }
  
  // factory methods for the builder:

  /**
   * Instantiates a {@linkplain Builder builder} pre-populated with this query.
   */
  public Builder newBuilder() {
    return new Builder(this);
  }

  /**
   * Instantiates a {@linkplain Builder builder} for a "{@code SELECT *}" query.
   */
  public static Builder selectStar() {
    return new Builder(SelectClause.Star.INSTANCE);
  }

  /**
   * Instantiates a {@linkplain Builder builder} for a "{@code SELECT __key__}" query.
   */
  public static Builder selectKey() {
    return new Builder(SelectClause.Key.INSTANCE);
  }

  /**
   * Instantiates a {@linkplain Builder builder} for a "{@code SELECT <property list>}" query.
   * @param propertyList the property names ({@code <property list> := <property> [, <property> ...]})
   * @see #select(String...)
   */
  public static Builder select(List<String> propertyList) {
    return new Builder(new SelectClause.Projection(propertyList));
  }

  /**
   * Instantiates a {@linkplain Builder builder} for a "{@code SELECT <property list>}" query.
   * @param propertyList the property names ({@code <property list> := <property> [, <property> ...]})
   * @see #select(List)
   */
  public static Builder select(String... propertyList) {
    return new Builder(new SelectClause.Projection(Arrays.asList(propertyList)));
  }

  /**
   * Instantiates a {@linkplain Builder builder} for a "{@code SELECT DISTINCT <property list>}" query.
   * @param propertyList the property names ({@code <property list> := <property> [, <property> ...]})
   * @see #selectDistinct(String...)
   */
  public static Builder selectDistinct(List<String> propertyList) {
    return new Builder(new SelectClause.Projection(true, propertyList));
  }

  /**
   * Instantiates a {@linkplain Builder builder} for a "{@code SELECT DISTINCT <property list>}" query.
   * @param propertyList the property names ({@code <property list> := <property> [, <property> ...]})
   * @see #selectDistinct(List)
   */
  public static Builder selectDistinct(String... propertyList) {
    return new Builder(new SelectClause.Projection(true, Arrays.asList(propertyList)));
  }

  public static class Builder {
    private SelectClause selectClause;
    private FromClause fromClause;
    private WhereClause whereClause;
    private OrderByClause orderByClause;
    private LimitClause limitClause;
    private OffsetClause offsetClause;

    /**
     * Initializes the builder using the given SELECT clause.
     *
     * @see #selectStar()
     * @see #selectKey()
     * @see #select(List)
     * @see #selectDistinct(List)
     */
    Builder(SelectClause selectClause) {
      this.selectClause = Objects.requireNonNull(selectClause);
    }

    /**
     * Initializes the builder to match the given query.
     */
    Builder(GqlQuery query) {
      this.selectClause = Objects.requireNonNull(query.selectClause);
      this.fromClause = query.fromClause;
      this.whereClause = query.whereClause;
      this.orderByClause = query.orderByClause;
      this.limitClause = query.limitClause;
      this.offsetClause = query.offsetClause;
    }

    /**
     * Sets the {@code [FROM <kind>]} clause.
     *
     * @param kind name of a datastore entity kind
     */
    public Builder from(String kind) {
      this.fromClause = new FromClause(kind);
      return this;
    }

    /**
     * Sets the {@code [WHERE <condition> [AND <condition> ...]]} clause.
     *
     * @param conditions the conditions in the {@code WHERE} clause
     */
    public Builder where(List<GqlCondition> conditions) {
      return where(new WhereClause(conditions));
    }

    /**
     * Sets the {@code [WHERE <condition> [AND <condition> ...]]} clause.
     *
     * @param whereClause the fully-formed {@code WHERE} clause
     */
    public Builder where(WhereClause whereClause) {
      this.whereClause = whereClause;
      return this;
    }

    /**
     * Sets the {@code [ORDER BY <property> [ASC | DESC] [, <property> [ASC | DESC] ...]]} clause.
     *
     * @param sortOrders the elements in the {@code ORDER BY} clause
     */
    public Builder orderBy(List<SortOrder> sortOrders) {
      return orderBy(new OrderByClause(sortOrders));
    }

    /**
     * Sets the {@code [ORDER BY <property> [ASC | DESC] [, <property> [ASC | DESC] ...]]} clause.
     *
     * @param sortOrders the elements in the {@code ORDER BY} clause;
     * each item will be parsed using {@link SortOrder#valueOf(String)}
     */
    public Builder orderBy(String... sortOrders) {
      return orderBy(new OrderByClause(sortOrders));
    }

    /**
     * Sets the {@code [ORDER BY <property> [ASC | DESC] [, <property> [ASC | DESC] ...]]} clause.
     */
    public Builder orderBy(OrderByClause orderByClause) {
      this.orderByClause = orderByClause;
      return this;
    }

    /**
     * Sets the {@code [LIMIT [<offset>,]<count>]} clause to contain both an offset and a count
     *
     * @see LimitClause#LimitClause(int, int)
     */
    public Builder limit(int offset, int count) {
      return limit(new LimitClause(offset, count));
    }

    /**
     * Sets the {@code [LIMIT [<offset>,]<count>]} clause to contain only a count
     *
     * @see LimitClause#LimitClause(int)
     */
    public Builder limit(int count) {
      return limit(new LimitClause(count));
    }

    /**
     * Sets the {@code [LIMIT [<offset>,]<count>]} clause.
     */
    public Builder limit(LimitClause limitClause) {
      // TODO: print a warning (or throw ISE) if this clause specifies an offset, but we already have a conflicting OFFSET clause?
      this.limitClause = limitClause;
      return this;
    }

    /**
     * Sets the {@code [OFFSET <offset>]} clause.
     */
    public Builder offset(int offset) {
      return offset(new OffsetClause(offset));
    }

    /**
     * Sets the {@code [OFFSET <offset>]} clause.
     */
    public Builder offset(OffsetClause offsetClause) {
      // TODO: print a warning (or throw ISE) if already have a LIMIT clause that specifies a conflicting offset?
      this.offsetClause = offsetClause;
      return this;
    }

    public GqlQuery build() {
      GqlQuery query = new GqlQuery();
      query.selectClause = this.selectClause;
      query.fromClause = this.fromClause;
      query.whereClause = this.whereClause;
      query.orderByClause = this.orderByClause;
      query.limitClause = this.limitClause;
      query.offsetClause = this.offsetClause;
      return query;
    }
  }


  /**
   * Represents the {@code SELECT [DISTINCT] [* | <property list> | __key__]} clause of a GQL query.
   *
   * @see <a href="https://cloud.google.com/appengine/docs/standard/python/datastore/gqlreference">GQL Reference</a>
   */
  public static abstract class SelectClause implements GqlElement {

    /**
     * {@code SELECT [DISTINCT] <property list>}
     */
    public static class Projection extends SelectClause {
      private boolean distinct;
      private List<String> propertyList;

      public Projection(@Nonnull List<String> propertyList) {
        this(false, propertyList);
      }

      public Projection(boolean distinct, @Nonnull List<String> propertyList) {
        this.distinct = distinct;
        this.propertyList = ListUtils.requireNonEmpty(propertyList);  // TODO: make a defensive copy?
      }

      public boolean isDistinct() {
        return distinct;
      }

      public List<String> getPropertyList() {
        return propertyList;
      }

      @Override
      public String toGql() {
        StringBuilder ret = new StringBuilder("SELECT ");
        if (distinct)
          ret.append("DISTINCT ");
        return StringUtils.appendJoined(ret, ", ", propertyList.iterator()).toString();
      }

      @Override
      public boolean equals(Object o) {
        if (this == o)
          return true;
        if (o == null || getClass() != o.getClass())
          return false;
        Projection that = (Projection)o;
        if (distinct != that.distinct)
          return false;
        return propertyList.equals(that.propertyList);
      }

      @Override
      public int hashCode() {
        int result = (distinct ? 1 : 0);
        result = 31 * result + propertyList.hashCode();
        return result;
      }
    }

    /**
     * {@code SELECT *}
     */
    public static class Star extends SelectClause {

      /**
       * Singleton instance of this class
       */
      public static final Star INSTANCE = new Star();

      /**
       * Can't be instantiated; use the {@linkplain #INSTANCE singleton instance}.
       */
      private Star() {
      }

      @Override
      public String toGql() {
        return "SELECT *";
      }
    }

    /**
     * {@code SELECT __key__}
     */
    public static class Key extends SelectClause {

      /**
       * Singleton instance of this class
       */
      public static final Key INSTANCE = new Key();

      /**
       * Can't be instantiated; use the {@linkplain #INSTANCE singleton instance}.
       */
      private Key() {
      }

      @Override
      public String toGql() {
        return "SELECT __key__";
      }
    }
  }



  /**
   * Represents the {@code [FROM <kind>]} clause of a GQL query.
   *
   * @see <a href="https://cloud.google.com/appengine/docs/standard/python/datastore/gqlreference">GQL Reference</a>
   */
  public static class FromClause implements GqlElement {

    /**
     * The name of a datastore entity kind.
     */
    private String kind;

    /**
     * @param kind the name of a datastore entity kind
     */
    public FromClause(@Nonnull String kind) {
      this.kind = Objects.requireNonNull(kind);
    }

    public String getKind() {
      return kind;
    }

    @Override
    public String toGql() {
      return "FROM " + kind;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      FromClause that = (FromClause)o;
      return kind.equals(that.kind);
    }

    @Override
    public int hashCode() {
      return kind.hashCode();
    }
  }

  /**
   * Represents the {@code [WHERE <condition> [AND <condition> ...]]} clause of a GQL query.
   *
   * @see <a href="https://cloud.google.com/appengine/docs/standard/python/datastore/gqlreference">GQL Reference</a>
   */
  public static class WhereClause implements GqlElement {
    private List<GqlCondition> conditions;

    /**
     * @param conditions the conditions in the {@code WHERE} clause
     */
    public WhereClause(@Nonnull List<GqlCondition> conditions) {
      this.conditions = ListUtils.requireNonEmpty(conditions);  // TODO: make a defensive copy?
    }

    public List<GqlCondition> getConditions() {
      return conditions;
    }

    @Override
    public String toGql() {
      return conditions.stream().map(GqlElement::toGql)
          .collect(Collectors.joining("AND", "WHERE ", ""));
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      WhereClause that = (WhereClause)o;
      return conditions.equals(that.conditions);
    }

    @Override
    public int hashCode() {
      return conditions.hashCode();
    }
  }

  /**
   * Represents the {@code [ORDER BY <property> [ASC | DESC] [, <property> [ASC | DESC] ...]]} clause of a GQL query.
   *
   * @see <a href="https://cloud.google.com/appengine/docs/standard/python/datastore/gqlreference">GQL Reference</a>
   */
  public static class OrderByClause implements GqlElement {
    private List<SortOrder> sortOrders;

    /**
     * @param sortOrders the elements in the {@code ORDER BY} clause
     */
    public OrderByClause(List<SortOrder> sortOrders) {
      this.sortOrders = ListUtils.requireNonEmpty(sortOrders);  // TODO: make a defensive copy?
    }

    /**
     * @param sortOrders the elements in the {@code ORDER BY} clause;
     *     each item will be parsed using {@link SortOrder#valueOf(String)}
     * @throws NullPointerException     if any element is {@code null}
     * @throws IllegalArgumentException if array is empty or if {@link SortOrder#valueOf(String)} is unable to parse one
     *                                  of the elements
     */
    public OrderByClause(String... sortOrders) {
      this(Arrays.stream(sortOrders).map(SortOrder::valueOf).collect(Collectors.toList()));
    }

    public List<SortOrder> getSortOrders() {
      return sortOrders;
    }

    @Override
    public String toGql() {
      return sortOrders.stream().map(GqlElement::toGql)
          .collect(Collectors.joining(",", "ORDER BY ", ""));
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      OrderByClause that = (OrderByClause)o;
      return sortOrders.equals(that.sortOrders);
    }

    @Override
    public int hashCode() {
      return sortOrders.hashCode();
    }
  }

  /**
   * Represents the {@code [LIMIT [<offset>,]<count>]} clause of a GQL query.
   *
   * <blockquote cite="https://cloud.google.com/appengine/docs/standard/python/datastore/gqlreference">
   *   An optional LIMIT clause causes the query to stop returning results after the first {@code <count>} entities.
   *   The LIMIT clause can also include an {@code <offset>}, to skip that many results to find the first result to return.
   *   An optional OFFSET clause can specify an {@code <offset>}, if no LIMIT clause is present.
   * </blockquote>
   *
   * @see <a href="https://cloud.google.com/appengine/docs/standard/python/datastore/gqlreference">GQL Reference</a>
   */
  public static class LimitClause implements GqlElement {
    /**
     * Optional "offset", which can be specified in the {@code LIMIT} clause if there is no {@code OFFSET} clause.
     */
    @Nullable
    private Integer offset;
    private int count;

    public LimitClause(int offset, int count) {
      this.offset = offset;
      this.count = count;
    }

    public LimitClause(int count) {
      this.count = count;
    }

    @Nullable
    public Integer getOffset() {
      return offset;
    }

    public int getCount() {
      return count;
    }

    @Override
    public String toGql() {
      StringBuilder ret = new StringBuilder("LIMIT ");
      if (offset != null)
        ret.append(offset).append(',');
      ret.append(count);
      return ret.toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      LimitClause that = (LimitClause)o;
      if (count != that.count)
        return false;
      return offset != null ? offset.equals(that.offset) : that.offset == null;
    }

    @Override
    public int hashCode() {
      int result = offset != null ? offset.hashCode() : 0;
      result = 31 * result + count;
      return result;
    }
  }

  /**
   * Represents the {@code [OFFSET <offset>]} clause of a GQL query.
   *
   * <blockquote cite="https://cloud.google.com/appengine/docs/standard/python/datastore/gqlreference">
   *   An optional OFFSET clause can specify an {@code <offset>}, if no LIMIT clause is present.
   * </blockquote>
   *
   * @see <a href="https://cloud.google.com/appengine/docs/standard/python/datastore/gqlreference">GQL Reference</a>
   */
  public static class OffsetClause implements GqlElement {
    private int offset;

    public OffsetClause(int offset) {
      this.offset = offset;
    }

    public int getOffset() {
      return offset;
    }

    @Override
    public String toGql() {
      return "OFFSET " + offset;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      OffsetClause that = (OffsetClause)o;
      return offset == that.offset;
    }

    @Override
    public int hashCode() {
      return offset;
    }
  }
}
