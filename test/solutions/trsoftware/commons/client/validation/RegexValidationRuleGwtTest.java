package solutions.trsoftware.commons.client.validation;

/**
 *
 * Date: Jul 7, 2008
 * Time: 2:03:21 PM
 * @author Alex
 */
public class RegexValidationRuleGwtTest extends RegexValidationRuleGwtTestCase {

  public void testRegexValidation() throws Exception {
    // 1) test with a single regex
    {
      RegexValidationRule rule = new RegexValidationRule("test", false, "[a-z]{2,4}");
      assertValidity(rule, "ASDF", false);
      assertValidity(rule, "a", false);
      assertValidity(rule, "as", true);
      assertValidity(rule, "as", true);
      assertValidity(rule, "asd", true);
      assertValidity(rule, "asdf", true);
      assertValidity(rule, "asdfe", false);
    }
    // 2) test with multiple regexes (all must pass)
    {
      RegexValidationRule rule = new RegexValidationRule("test", false, "[a-z]{2,4}", "[asdf]*");
      assertValidity(rule, "ASDF", false);
      assertValidity(rule, "a", false);  // too short
      assertValidity(rule, "as", true);
      assertValidity(rule, "xx", false); // doesn't match [asdf]*
      assertValidity(rule, "as", true);
      assertValidity(rule, "asd", true);
      assertValidity(rule, "asdf", true);
      assertValidity(rule, "asde", false);  // doesn't match [asdf]*
      assertValidity(rule, "asdfe", false);  // too long
    }
  }


}