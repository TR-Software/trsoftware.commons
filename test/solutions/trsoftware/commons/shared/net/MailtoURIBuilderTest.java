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

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static solutions.trsoftware.commons.shared.net.MailtoURIBuilder.*;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;

/**
 * @author Alex
 * @since 5/1/2021
 */
public class MailtoURIBuilderTest extends TestCase {

  private static final String EMPTY_MAILTO = "mailto:";

  public void testEscapeAddress() throws Exception {
    // test some examples given in https://tools.ietf.org/html/rfc6068#section-6
    assertEquals("addr1@an.example", escapeAddress("addr1@an.example"));
    /*
     According to [RFC5322], the characters "?", "&", and even "%" may
     occur in addr-specs.  The fact that they are reserved characters is
     not a problem: those characters may appear in 'mailto' URIs -- they
     just may not appear in unencoded form.  The standard URI encoding
     mechanisms ("%" followed by a two-digit hex number) MUST be used in
     these cases.
     */
    assertEquals("gorby%25kremvax@example.com", escapeAddress("gorby%kremvax@example.com"));
    assertEquals("unlikely%3Faddress@example.com", escapeAddress("unlikely?address@example.com"));
    assertEquals("Mike%26family@example.org", escapeAddress("Mike&family@example.org"));
    assertEquals("%22not%40me%22@example.org", escapeAddress("\"not@me\"@example.org"));
    assertEquals("%22oh%5C%5Cno%22@example.org", escapeAddress("\"oh\\\\no\"@example.org"));
    assertEquals("%22%5C%5C%5C%22it's%5C%20ugly%5C%5C%5C%22%22@example.org", escapeAddress("\"\\\\\\\"it's\\ ugly\\\\\\\"\"@example.org"));

    // the <domain> part of the address has more permissive rules than the <local-part> (allows the ':' char)
    assertEquals("foo@example.com:1234", escapeAddress("foo@example.com:1234"));
  }

  public void testBuildString() throws Exception {
    // NOTE: examples were generated with https://www.rapidtables.com/web/html/mailto.html
    // 1) test with only standard fields, and no custom fields
    {
      checkResult(EMPTY_MAILTO,
          new MailtoURIBuilder());
      // with direct recipient
      checkResult("mailto:foo@example.com",
          new MailtoURIBuilder()
              .setTo("foo@example.com"));
      // no direct recipient, but has CC
      checkResult("mailto:?cc=foo@example.com",
          new MailtoURIBuilder()
              .setCc("foo@example.com"));
      // with direct recipient, multi-CC, and subject (also note the legal '+' chars in email addresses
      checkResult("mailto:foo+bar@example.com?cc=bar+1@example.com,baz+2@example.com&subject=Hello%20world",
          new MailtoURIBuilder()
              .setTo("foo+bar@example.com")
              .setCc("bar+1@example.com", "baz+2@example.com")
              .setSubject("Hello world")
      );
      // with direct recipient, multi-CC, subject, and multiline body
      checkResult("mailto:foo@example.com?cc=bar@example.com,baz@example.com&subject=Hello%20world&body=There%20is%20no%20pain%20you%20are%20receding.%0D%0A%0D%0AA%20distant%20ship%20smoke%20on%20the%20horizon.",
          new MailtoURIBuilder()
              .setTo("foo@example.com")
              .setCc("bar@example.com", "baz@example.com")
              .setSubject("Hello world")
              .setBody("There is no pain you are receding.\r\n\r\nA distant ship smoke on the horizon.")
      );
      // with multi-TO, multi-CC, multi-BCC, subject, and multiline body with non-ASCII chars
      checkResult("mailto:foo@example.com,foobar@example.com?cc=bar@example.com,baz@example.com&bcc=x@example.com,y@example.com&subject=%D0%9F%D1%80%D0%B8%D0%B2%D0%B5%D1%82&body=%D0%9F%D1%80%D0%B8%D0%B2%D0%B5%D1%82!%0D%0A%0D%0A%D0%9A%D0%B0%D0%BA%20%D0%B4%D0%B5%D0%BB%D0%B0%3F",
          new MailtoURIBuilder()
              .setTo("foo@example.com", "foobar@example.com")
              .setCc("bar@example.com", "baz@example.com")
              .setBcc(Arrays.asList("x@example.com", "y@example.com"))
              .setSubject("Привет")
              .setBody("Привет!\r\n\r\nКак дела?")
      );
      // example from TypeRacer (racetrack invitation message)
      checkResult("mailto:?subject=Race%20me%20on%20TypeRacer&body=Use%20this%20link%20to%20join%20my%20private%20racetrack%20on%20TypeRacer%20(the%20online%20typing%20competition)%3A%20%0A%0Ahttp%3A%2F%2Flocalhost%3A8081%3Frt%3D25r0nhnpdc",
          new MailtoURIBuilder()
                  .setSubject("Race me on TypeRacer")
                  .setBody("Use this link to join my private racetrack on TypeRacer (the online typing competition): \n\nhttp://localhost:8081?rt=25r0nhnpdc"));
    }
  }

  private void checkResult(String expected, MailtoURIBuilder builder) {
    System.out.println("Expecting " + expected);
    assertEquals(expected, builder.buildString());
  }

  public void testFieldAccessors() throws Exception {
    MailtoURIBuilder builder = new MailtoURIBuilder();

    // test single-valued To field (using varargs setter)
    builder.setTo("foo@example.com");
    MessageField expected = new AddressListField("to", Collections.singletonList("foo@example.com"));
    assertEquals(expected, builder.getField("to"));
    assertEquals(expected, builder.getField("To"));  // should be case-insensitive
    assertEquals(expected, builder.getField(StandardFieldName.to));
    assertEquals("mailto:foo@example.com", builder.buildString());
    // test field removal:
    //   a) by passing null array to varargs setter
    builder.setTo((String[])null);
    assertNull(builder.getField("to"));
    assertEquals(EMPTY_MAILTO, builder.buildString());
    //   b) by passing null to List setter
    builder.setTo("foo@example.com");
    assertEquals(expected, builder.getField("to"));
    builder.setTo((List<String>)null);
    assertNull(builder.getField("to"));
    //   c) by passing empty array to varargs setter
    builder.setTo("foo@example.com");
    assertEquals(expected, builder.getField("to"));
    builder.setTo();
    assertNull(builder.getField("to"));
    //   d) by passing empty list to list setter
    builder.setTo("foo@example.com");
    assertEquals(expected, builder.getField("to"));
    builder.setTo(Collections.emptyList());
    assertNull(builder.getField("to"));
    //   e) by calling removeField
    builder.setTo("foo@example.com");
    assertEquals(expected, builder.getField("to"));
    builder.removeField("TO"); // should be case-insensitive
    assertNull(builder.getField("to"));

    // test multi-valued To field (using varargs setter)
    builder.setTo("foo@example.com", "bar@example.com");
    expected = new AddressListField("to", Arrays.asList("foo@example.com", "bar@example.com"));
    assertEquals(expected, builder.getField("to"));
    builder.removeField(StandardFieldName.to);
    assertNull(builder.getField("to"));

    // test multi-valued To field (using List setter)
    builder.setTo(Arrays.asList("foo@example.com", "bar@example.com", "baz@example.com"));
    expected = new AddressListField("to", Arrays.asList("foo@example.com", "bar@example.com", "baz@example.com"));
    assertEquals(expected, builder.getField("To"));  // should be case-insensitive
    assertEquals(expected, builder.getField(StandardFieldName.to));
    builder.removeField(StandardFieldName.to);
    assertNull(builder.getField("to"));

    // NOTE: we're not testing the setters for CC and BCC because their implementation is the same as TO

    // test the generic setters
    builder.setField(expected);
    assertEquals(expected, builder.getField(StandardFieldName.to));

    expected = new SimpleField("foo", "bar");
    builder.setField(expected);
    assertEquals(expected, builder.getField("Foo"));  // should be case-insensitive
    assertEquals(expected, builder.removeField("FOO"));
    assertNull(builder.getField("foo"));

    builder.setField("foo", "bar");
    assertEquals(expected, builder.getField("Foo"));  // should be case-insensitive
    assertEquals(expected, builder.removeField("FOO"));
    assertNull(builder.getField("foo"));

    // Test some bad inputs that should throw an exception:

    // the setField(String, String) and SimpleField constructor method cannot be used for the to/cc/bcc fields
    assertThrows(IllegalArgumentException.class, () -> builder.setField("to", "foo@example.com"));
    assertThrows(IllegalArgumentException.class, () -> new SimpleField("to", "foo@example.com"));

    // the address list setters should throw an exception if the list contains any null elements
    assertThrows(IllegalArgumentException.class, () -> builder.setTo((String)null));
    assertThrows(IllegalArgumentException.class, () -> builder.setTo("foo@example.com", null));
    assertThrows(IllegalArgumentException.class, () -> builder.setTo(Collections.singletonList(null)));
    assertThrows(IllegalArgumentException.class, () -> builder.setTo(Arrays.asList("foo@example.com", null)));
  }

}