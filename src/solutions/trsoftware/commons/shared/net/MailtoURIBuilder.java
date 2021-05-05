/*
 * Copyright 2021 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.commons.shared.net;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.net.PercentEscaper;
import solutions.trsoftware.commons.client.bridge.util.URIComponentEncoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Helper class for constructing "{@code mailto:}" URIs
 * (for example: {@code mailto:nowhere@mozilla.org?cc=nobody@mozilla.org&subject=This%20is%20the%20subject}).
 *
 * @see <a href="https://tools.ietf.org/html/rfc6068">RFC 6068: The 'mailto' URI Scheme</a>
 * @see <a href="https://en.wikipedia.org/wiki/Mailto">Wikipedia</a>
 * @see <a href="https://mailtrap.io/blog/mailto-links-explained/">Mailto HTML Links Explained</a>
 *
 * @author Alex
 * @since 4/28/2021
 */
public class MailtoURIBuilder {

  /**
   * A name-field mapping representing the current state of the builder.  The names should be lowercase, to
   * allow case-insensitive lookups.
   */
  private final LinkedHashMap<String, MessageField<?>> fields = new LinkedHashMap<>();

  /**
   * Generates the "{@code mailto:}" URI string representing the current state of this builder.
   * <p>
   * <strong>Caution</strong>: the result may need further escaping, depending on context.
   * For example: the {@code '&'} character is reserved in HTML and XML, and may need to be
   * written with an HTML/XML entity ("&amp;") or numeric character reference ("&#x26;" or "&#38;").
   *
   * @see <a href="https://tools.ietf.org/html/rfc6068">RFC 6068 (The 'mailto' URI Scheme)</a>
   */
  public String buildString() {
    StringBuilder out = new StringBuilder("mailto:");
    AddressListField toField = (AddressListField)getField(StandardFieldName.to);
    if (toField != null) {
      // the "to" component is a special case, in that it comes before the '?' char and doesn't have a name
      out.append(toField.encodeValue());
    }
    char sep = '?';
    for (MessageField<?> field : fields.values()) {
      if (field != toField) {
        out.append(sep).append(field.encode());
        sep = '&';
      }
    }
    return out.toString();
  }

  /**
   * Escapes an email address according to the rules for {@code <addr-spec>} defined in
   * <a href="https://tools.ietf.org/html/rfc6068#section-2">RFC5322 §2.1</a>, which
   * allows the chars {@code '@'} and {@code ':'} to remain unescaped.
   */
  @VisibleForTesting
  static String escapeAddress(String address) {
    int iAt = address.lastIndexOf('@');
    if (iAt < 1)
      throw new IllegalArgumentException("Invalid email address: " + address);
    String localPart = address.substring(0, iAt);
    String domain = address.substring(iAt+1);
    return addressLocalPartEscaper.escape(localPart) + '@' + addressDomainEscaper.escape(domain);
  }

  /**
   * Implements (or attempts to implement) the escaping rules for the {@code <local-part>} of an {@link <addr-spec>},
   * as specified in <a href="https://tools.ietf.org/html/rfc6068#section-2">RFC5322 §2.1</a>.
   * <p>
   * Like {@link URIComponentEncoder}, but also treats the {@code '+'} char as safe.
   */
  private static final PercentEscaper addressLocalPartEscaper = new PercentEscaper(
      "!'()*-._~+",
      false
  );

  /**
   * Implements (or attempts to implement) the escaping rules for the {@code <domain>} part of an {@link <addr-spec>},
   * as specified in <a href="https://tools.ietf.org/html/rfc6068#section-2">RFC5322 §2.1</a>.
   * <p>
   * Slightly more permissive than {@link #addressLocalPartEscaper} because it treats the {@code '@'} and  {@code ':'}
   * chars as be safe.
   *
   * Like {@link URIComponentEncoder}, but also treats the {@code '+'} char as safe.
   */
  private static final PercentEscaper addressDomainEscaper = new PercentEscaper(
      "!'()*-._~+@:",
      false
  );

  /**
   * Escapes an normal (non-address) field value according to the rules for {@code <addr-spec>} defined in
   * <a href="https://tools.ietf.org/html/rfc6068#section-2">RFC5322 §2</a>, which is basically equivalent
   * to the Javascript {@code encodeURIComponent} function.
   *
   * @see #escapeAddress(String)
   */
  @VisibleForTesting
  static String escapeURIComponent(String value) {
    return URIComponentEncoder.getInstance().encode(value);
  }

  /**
   * Emits a comma-separated list of the given email addresses, according to the syntax
   * specified in <a href="https://tools.ietf.org/html/rfc5322#section-3.4">RFC5322 §3.4</a>:
   * <pre>
   *   address-list    =   (address *("," address)) / obs-addr-list
   * </pre>
   */
  @VisibleForTesting
  static String encodeAddressList(List<String> addressList) {
    return addressList.stream().map(MailtoURIBuilder::escapeAddress).collect(Collectors.joining(","));
  }

  /**
   * Can be used to set a non-{@linkplain StandardFieldName standard} mailto field.
   * <p>
   * For all standard fields, should use the explicit setters instead:
   * {@link #setTo(String...)}, {@link #setCc(String...)}, {@link #setBcc(String...)}, {@link #setSubject(String)},
   * and {@link #setBody(String)}.
   *
   * @param field contains both the name and value of the field to set
   *   <strong>NOTE: </strong> must ensure that any address-containing fields (such as {@code "to"}, {@code "cc"}, {@code "bcc"})
   *   are represented by an instance of {@link AddressListField}
   */
  public MailtoURIBuilder setField(@Nonnull MessageField<?> field) {
    fields.put(field.getName(), field);
    return this;
  }

  /**
   * Can be used to set a non-{@linkplain StandardFieldName standard} mailto field with a single string value,
   * creating a new instance of {@link SimpleField} for the given name/value pair.
   * <p>
   * For all standard fields, should use the explicit setters instead:
   * {@link #setTo(String...)}, {@link #setCc(String...)}, {@link #setBcc(String...)}, {@link #setSubject(String)},
   * and {@link #setBody(String)}.
   * <p>
   * <em>NOTE:</em> this method cannot be used for any address-containing fields
   * (such as {@code "to"}, {@code "cc"}, {@code "bcc"}, {@code "reply-to"}, etc.)
   * because they follow different URL-encoding rules than simple fields.
   */
  public MailtoURIBuilder setField(@Nonnull String name, @Nonnull String value) {
    return setField(new SimpleField(name, value));
  }

  /**
   * @param name case-insensitive field name
   * @return current value of the field with the given name, or {@code null} if no such field is present
   */
  @Nullable
  public MessageField<?> getField(@Nonnull String name) {
    return fields.get(name.toLowerCase());
  }

  /**
   * @return current value of the field with the given name, or {@code null} if no such field is present
   */
  @Nullable
  public MessageField<?> getField(@Nonnull StandardFieldName name) {
    return getField(name.name());
  }

  /**
   * Removes a field from the current mailto URI, if present.
   *
   * @param name name of the field to be removed, as a case-insensitive string
   * @return the previous value of this field, or {@code null} if it didn't exist
   */
  public MessageField<?> removeField(@Nonnull String name) {
    return fields.remove(name.toLowerCase());
  }

  /**
   * Removes a field from the current mailto URI, if present.
   *
   * @param name name of the field to be removed
   * @return the previous value of this field, or {@code null} if it didn't exist
   */
  public MessageField<?> removeField(@Nonnull StandardFieldName name) {
    return removeField(name.name());
  }

  private MailtoURIBuilder setAddressField(StandardFieldName name, String... emailAddresses) {
    return setAddressField(name, emailAddresses == null ? null : Arrays.asList(emailAddresses));
  }

  private MailtoURIBuilder setAddressField(StandardFieldName name, List<String> emailAddresses) {
    if (emailAddresses == null || emailAddresses.isEmpty())
      removeField(name.name());
    else
      setField(new AddressListField(name.name(), emailAddresses));
    return this;
  }

  /**
   * Sets the "To" address(es).
   *
   * @param emailAddresses one or more recipient addresses; if {@code null} or empty, the field will be removed.
   */
  public MailtoURIBuilder setTo(String... emailAddresses) {
    return setAddressField(StandardFieldName.to, emailAddresses);
  }

  /**
   * Sets the "To" address(es).
   *
   * @param emailAddresses one or more recipient addresses; if {@code null} or empty, the field will be removed.
   */
  public MailtoURIBuilder setTo(List<String> emailAddresses) {
    return setAddressField(StandardFieldName.to, emailAddresses);
  }

  /**
   * Sets the "Cc" address(es).
   *
   * @param emailAddresses one or more recipient addresses; if {@code null} or empty, the field will be removed.
   */
  public MailtoURIBuilder setCc(String... emailAddresses) {
    return setAddressField(StandardFieldName.cc, emailAddresses);
  }

  /**
   * Sets the "Cc" address(es).
   *
   * @param emailAddresses one or more recipient addresses; if {@code null} or empty, the field will be removed.
   */
  public MailtoURIBuilder setCc(List<String> emailAddresses) {
    return setAddressField(StandardFieldName.cc, emailAddresses);
  }

  /**
   * Sets the "Bcc" address(es).
   *
   * @param emailAddresses one or more recipient addresses; if {@code null} or empty, the field will be removed.
   */
  public MailtoURIBuilder setBcc(String... emailAddresses) {
    return setAddressField(StandardFieldName.bcc, emailAddresses);
  }

  /**
   * Sets the "Bcc" address(es).
   *
   * @param emailAddresses one or more recipient addresses; if {@code null} or empty, the field will be removed.
   */
  public MailtoURIBuilder setBcc(List<String> emailAddresses) {
    return setAddressField(StandardFieldName.bcc, emailAddresses);
  }


  /**
   * Sets the "Subject" field.
   */
  public MailtoURIBuilder setSubject(String subject) {
    return setField(StandardFieldName.subject.name(), subject);
  }

  /**
   * Sets the "Subject" field.
   * @param body the body text; linebreaks should be represented as {@code "\r\n"}
   * @see <a href="https://tools.ietf.org/html/rfc6068#section-5">RFC6068 §5</a>
   */
  public MailtoURIBuilder setBody(String body) {
    return setField(StandardFieldName.body.name(), body);
  }

  /**
   * Enumerates the most-common field names in a "mailto" URI.
   */
  public enum StandardFieldName {
    to, cc, bcc, subject, body;
  }

  /**
   * Represents a name-value pair that will be included in the "mailto" URI generated by {@link #buildString()}.
   * <p>
   * The name attribute is stored as a lowercase string, to allow case-insensitive lookups.
   * All implementations of this class should be immutable.
   *
   * @param <V>
   */
  public abstract static class MessageField<V> {
    protected final String name;
    protected final V value;

    /**
     * @param name the field name; will be converted to lowercase.
     * @param value the field value
     */
    protected MessageField(@Nonnull String name, @Nonnull V value) {
      this.name = Objects.requireNonNull(name, "name").toLowerCase();
      this.value = Objects.requireNonNull(value, "value");
    }

    String encode() {
      StringBuilder ret = new StringBuilder();
      if (!StandardFieldName.to.name().equalsIgnoreCase(name)) {
        // the value of the "to" field goes right after the "mailto:" prefix instead of being included
        // as a name-value pair in the URI's query string
        ret.append(escapeURIComponent(name)).append('=');
      }
      ret.append(encodeValue());
      return ret.toString();
    }

    protected abstract String encodeValue();

    public String getName() {
      return name;
    }

    public V getValue() {
      return value;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("name", name)
          .add("value", value)
          .toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      MessageField<?> that = (MessageField<?>)o;

      if (!name.equals(that.name))
        return false;
      return value.equals(that.value);
    }

    @Override
    public int hashCode() {
      int result = name.hashCode();
      result = 31 * result + value.hashCode();
      return result;
    }
  }

  /**
   * Represents a simple, non address-containing field, such as {@code subject} or {@code body}.
   */
  public static class SimpleField extends MessageField<String> {

    /**
     * @param name the field name; will be converted to lowercase.
     * @param value the field value
     * @throws IllegalArgumentException if the name is {@code "to"}, {@code "cc"}, or {@code "bcc"}:
     * those fields should be represented by instances of {@link AddressListField} rather than {@link SimpleField}.
     */
    public SimpleField(@Nonnull String name, @Nonnull String value) {
      super(name, value);
      // make sure this is not an address-containing field: to/cc/bcc have different encoding logic from simple fields
      StandardFieldName[] addrFields = {StandardFieldName.to, StandardFieldName.cc, StandardFieldName.bcc};
      for (StandardFieldName addrField : addrFields) {
        if (addrField.name().equalsIgnoreCase(name))
          throw new IllegalArgumentException(Strings.lenientFormat(
              "The '%s' field can't be an instance of %s; use an instance of %s instead",
              name, SimpleField.class.getSimpleName(), AddressListField.class.getSimpleName()));
      }
    }

    @Override
    protected String encodeValue() {
      return escapeURIComponent(value);
    }
  }

  /**
   * Represents a mail field like {@code "to"}, {@code "cc"}, and {@code "bcc"}, which can contain a comma-separated list
   * of email addresses, encoded according to the syntax specified in <a href="https://tools.ietf.org/html/rfc5322#section-3.4">RFC5322 §3.4</a>:
   * <pre>
   *   address-list    =   (address *("," address)) / obs-addr-list
   * </pre>
   * The URL-encoding rules for these fields also differ from standard fields (as specified in
   * <a href="https://tools.ietf.org/html/rfc6068#section-2">RFC6068 §2.1</a>).
   */
  public static class AddressListField extends MessageField<List<String>> {
    /**
     * @param name the field name; will be converted to lowercase.
     * @param addressList one or more recipient addresses
     *
     * @throws NullPointerException if the list is {@code null}
     * @throws IllegalArgumentException if the list contains any {@code null} elements
     */
    public AddressListField(@Nonnull String name, @Nonnull List<String> addressList) {
      super(name, addressList);
      if (addressList.stream().anyMatch(Objects::isNull)) {
        throw new IllegalArgumentException("addressList must not contain any null elements");
      }
    }

    @Override
    protected String encodeValue() {
      return encodeAddressList(value);
    }
  }

}
