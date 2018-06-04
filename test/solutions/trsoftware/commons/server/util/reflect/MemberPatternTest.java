/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.server.util.reflect;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.collections.DefaultMap;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;

import static solutions.trsoftware.commons.server.util.reflect.MemberPattern.*;

public class MemberPatternTest extends TestCase {
  protected interface IFoo {
     int getFoo();

     IFoo IMPL = new Foo(0, "xyz");

     boolean equals(Object o);
   }

   protected static class Foo implements IFoo {
     public transient int x;
     public final int foo;
     public String bar;

     private Foo(int foo, String bar) {
       this.foo = foo;
       this.bar = bar;
     }

     @Override
     public int getFoo() {
       return foo;
     }

     public String getBar() {
       return bar;
     }

     public void setBar(String bar) {
       this.bar = bar;
     }
   }

   protected static class Bar {
     public transient int x;
     public final int foo;
     public String bar;

     private Bar(int foo, String bar) {
       this.foo = foo;
       this.bar = bar;
     }

     public int getFoo() {
       return foo;
     }

     public String getBar() {
       return bar;
     }

     public void setBar(String bar) {
       this.bar = bar;
     }
   }

   protected List<Field> allFields;
   protected List<Method> allMethods;
   protected List<Member> allMembers;
   protected DefaultMap<MemberPattern, Multimap<Boolean, Member>> results;
   protected static final List<Class> classes = Collections.unmodifiableList(Arrays.<Class>asList(IFoo.class, Foo.class));

   public void setUp() throws Exception {
     super.setUp();
     allFields = new ArrayList<>();
     allMethods = new ArrayList<>();
     allMembers = new ArrayList<>();
     for (Class cls : classes) {
       for (Field field : cls.getFields()) {
         allFields.add(field);
         allMembers.add(field);
       }
       for (Method method : cls.getMethods()) {
         allMethods.add(method);
         allMembers.add(method);
       }
     }
     results = new DefaultMap<MemberPattern, Multimap<Boolean, Member>>(new HashMap<>()) {
       @Override
       public Multimap<Boolean, Member> computeDefault(MemberPattern key) {
         return LinkedHashMultimap.create();
       }
     };
   }

   public void tearDown() throws Exception {
     printTestResults();
     verifyCommonAssertions(results.keySet().toArray(new MemberPattern[results.size()]));
     allFields = null;
     allMethods = null;
     allMembers = null;
     results = null;
     super.tearDown();
   }

   private void printTestResults() throws InterruptedException {
     System.out.println(getName() + " results:");
     int i = 1;
     for (MemberPattern key : results.keySet()) {
       System.out.printf("%d. %s%n", i++, key);
       for (boolean matches: new boolean[]{true, false}) {
         Collection<Member> members = results.get(key).get(matches);
         System.out.println("  " + (matches ? "matches" : "doesn't match") + ":");
         if (members.isEmpty())
           System.out.println("    <empty set>");
         else {
           for (Member member : members) {
             System.out.printf("    %s %30s     <%s>%n", matches ? "+" : "-", ReflectionUtils.toString(member), member);
           }
         }
       }
     }
     // flush the output stream and wait a few millis so that the IDE is able to display the full output with the matching test
     System.out.flush();
     Thread.sleep(2);
   }

   protected void assertMatches(MemberPattern predicate, Member member, boolean matches) {
     String errMsg = String.format("predicate=%s, member=%s", predicate, ReflectionUtils.toString(member));
     assertEquals(errMsg, matches, predicate.apply(member));
     results.get(predicate).put(matches, member);
   }

   protected final void assertMatches(MemberPattern predicate, Member member) {
     assertMatches(predicate, member, true);
   }

   protected final void assertNotMatches(MemberPattern predicate, Member member) {
     assertMatches(predicate, member, false);
   }


  private void verifyCommonAssertions(MemberPattern... patterns) {
    for (MemberPattern pattern : patterns) {
      // 1) assert that none of the patterns match null
      assertNotMatches(pattern, null);
      // 2) assert the inverse (NOT) of any pattern behaves accordingly
    }
    // 3) assert that the disjunction (AND), and conjunction (OR) of any of the tested patterns behaves accordingly
    for (MemberPattern p1 : patterns) {
      checkNot(p1);
      for (MemberPattern p2 : patterns) {
        MemberPattern p1AndP2 = MemberPattern.and(p1, p2);
        MemberPattern p1OrP2 = MemberPattern.or(p1, p2);
        for (Member member : allMembers) {
          assertMatches(p1AndP2, member, p1.matches(member) && p2.matches(member));
          assertMatches(p1OrP2, member, p1.matches(member) || p2.matches(member));
        }
      }
    }
  }

  private void checkNot(MemberPattern p) {
    MemberPattern np = MemberPattern.not(p);
    for (Member member : allMembers) {
      assertMatches(np, member, !p.matches(member));
    }
  }


  /** Tests the {@link MemberPattern.MemberType} filter. */
  public void testMemberTypePattern() throws Exception {
    for (Field field : allFields) {
      assertMatches(MemberType.FIELD, field);
      assertNotMatches(MemberType.METHOD, field);
    }
    for (Method method : allMethods) {
      assertMatches(MemberType.METHOD, method);
      assertNotMatches(MemberType.FIELD, method);
    }
  }

  /** Tests the {@link NameMatches} filter. */
  public void testNameMatchesPattern() throws Exception {
    // 1) test the Name.EVERYTHING and Name.NOTHING regexes
    NameMatches everything = new NameMatches(NameMatches.EVERYTHING);
    NameMatches nothing = new NameMatches(NameMatches.NOTHING);
    for (Member member : allMembers) {
      assertMatches(everything, member);
      assertNotMatches(nothing, member);
    }
    // 2) test some specific regexes
    String regex = "set.*";
    for (NameMatches setterPattern : Arrays.asList(
        new NameMatches(Pattern.compile(regex)),
        MemberPattern.nameMatches(regex),
        MemberPattern.nameMatches(Pattern.compile(regex)))) {
      for (Member member : allMembers)
        assertMatches(setterPattern, member, member.getName().startsWith("set"));
    }
    regex = ".{2,}";
    for (NameMatches moreThanOneCharPattern : Arrays.asList(
        new NameMatches(Pattern.compile(regex)),
        MemberPattern.nameMatches(regex),
        MemberPattern.nameMatches(Pattern.compile(regex)))) {
      for (Member member : allMembers)
        assertMatches(moreThanOneCharPattern, member, member.getName().length() > 1);
    }
  }

  /** Tests the {@link MemberPattern.Modifiers} filter. */
  public void testModifiersPattern() throws Exception {
    Modifiers staticPattern = new Modifiers(Modifier.STATIC);
    Modifiers publicPattern = new Modifiers(Modifier.PUBLIC);
    Modifiers publicTransientPattern = new Modifiers(Modifier.PUBLIC | Modifier.TRANSIENT);
    Modifiers publicFinalPattern = new Modifiers(Modifier.PUBLIC | Modifier.FINAL);
    for (Member member : allMembers) {
      int mods = member.getModifiers();
      assertMatches(staticPattern, member, Modifier.isStatic(mods));
      assertMatches(publicPattern, member, Modifier.isPublic(mods));
      assertMatches(publicTransientPattern, member, Modifier.isPublic(mods) && Modifier.isTransient(mods));
      assertMatches(publicFinalPattern, member, Modifier.isPublic(mods) && Modifier.isFinal(mods));
    }
  }

  /** Tests the {@link SpecificMembers} filter. */
  public void testSpecificMembersPattern() throws Exception {
    HashSet<Member> members = new HashSet<Member>(Arrays.asList(IFoo.class.getField("IMPL"), Foo.class.getMethod("setBar", String.class)));
    SpecificMembers patternFromArray = new SpecificMembers(members.toArray(new Member[members.size()]));
    SpecificMembers patternFromCollection = new SpecificMembers(members);
    for (Member member : allMembers) {
      assertMatches(patternFromArray, member, members.contains(member));
      assertMatches(patternFromCollection, member, members.contains(member));
    }
  }

  /** Tests the {@link MemberPattern.ParameterCount} filter. */
  public void testParameterCountPattern() throws Exception {
    for (int n = 0; n <= 3; n++) {
      ParameterCount pattern = new ParameterCount(n);
      for (Member member : allMembers)
        assertMatches(pattern, member, member instanceof Method && ((Method)member).getParameterTypes().length == n);
    }
  }

  /** Tests the {@link ValueType} filter. */
  public void testValueTypePattern() throws Exception {
    for (Class valueType : new Class[]{void.class, int.class, Integer.class, String.class}) {
      for (ValueType pattern : Arrays.asList(new ValueType(valueType), MemberPattern.valueTypeIs(valueType))) {
        for (Member member : allMembers)
          assertMatches(pattern, member,
              (member instanceof Method && ((Method)member).getReturnType() == valueType)
                  || (member instanceof Field && ((Field)member).getType() == valueType));
      }
    }
  }

  /** Tests the {@link InheritedFrom} filter. */
  public void testInheritedFromPattern() throws Exception {
    for (InheritedFrom pattern : new InheritedFrom[]{new InheritedFrom(Object.class), MemberPattern.inheritedFrom(Object.class)}) {
      assertNotMatches(pattern, Foo.class.getField("foo"));
      assertNotMatches(pattern, Foo.class.getMethod("getFoo"));
      assertMatches(pattern, Foo.class.getMethod("hashCode"));
      assertMatches(pattern, Foo.class.getMethod("equals", Object.class));
    }
  }


}