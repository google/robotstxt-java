// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.search.robotstxt;

import static org.junit.Assert.*;

import org.junit.Test;

/** Unit tests validating matching behavior. */
public class RobotsMatcherTest {
  private static RobotsMatcher parse(final String robotsTxtBody) {
    final Parser parser = new RobotsParser(new RobotsParseHandler());
    return (RobotsMatcher) parser.parse(robotsTxtBody);
  }

  /** Verifies: parsing an matching robots.txt containing single group */
  @Test
  public void singleGroup() {
    final String robotsTxtBodyCorrect = "user-agent: FooBot\n" + "disallow: /\n";
    final String robotsTxtBodyIncorrect = "foo: FooBot\n" + "bar: /\n";

    final String url = "http://foo.bar/x/y";

    try {
      final RobotsMatcher matcherCorrect = parse(robotsTxtBodyCorrect);
      assertFalse(matcherCorrect.singleAgentAllowedByRobots("FooBot", url));

      final RobotsMatcher matcherIncorrect = parse(robotsTxtBodyIncorrect);
      assertTrue(matcherIncorrect.singleAgentAllowedByRobots("FooBot", url));
    } catch (final MatchException e) {
      fail("Matcher has thrown an exception: " + e.getMessage());
    }
  }

  /**
   * Verifies: parsing an matching robots.txt containing multiple groups, invalid directives
   * ignorance.
   */
  @Test
  public void multipleGroups() {
    final String robotsTxtBody =
        "allow: /foo/bar/\n"
            + "\n"
            + "user-agent: FooBot\n"
            + "disallow: /\n"
            + "allow: /x/\n"
            + "user-agent: BarBot\n"
            + "disallow: /\n"
            + "allow: /y/\n"
            + "\n"
            + "\n"
            + "allow: /w/\n"
            + "user-agent: BazBot\n"
            + "\n"
            + "user-agent: FooBot\n"
            + "allow: /z/\n"
            + "disallow: /\n";

    final String urlWa = "http://foo.bar/w/a";
    final String urlXb = "http://foo.bar/x/b";
    final String urlYc = "http://foo.bar/y/c";
    final String urlZd = "http://foo.bar/z/d";
    final String urlFooBar = "http://foo.bar/foo/bar/";

    try {
      final RobotsMatcher matcher = parse(robotsTxtBody);

      assertTrue(matcher.singleAgentAllowedByRobots("FooBot", urlXb));
      assertTrue(matcher.singleAgentAllowedByRobots("FooBot", urlZd));
      assertFalse(matcher.singleAgentAllowedByRobots("FooBot", urlYc));
      assertTrue(matcher.singleAgentAllowedByRobots("BarBot", urlYc));
      assertTrue(matcher.singleAgentAllowedByRobots("BarBot", urlWa));
      assertFalse(matcher.singleAgentAllowedByRobots("BarBot", urlZd));
      assertTrue(matcher.singleAgentAllowedByRobots("BazBot", urlZd));

      assertFalse(matcher.singleAgentAllowedByRobots("FooBot", urlFooBar));
      assertFalse(matcher.singleAgentAllowedByRobots("BarBot", urlFooBar));
      assertFalse(matcher.singleAgentAllowedByRobots("BazBot", urlFooBar));
    } catch (final MatchException e) {
      fail("Matcher has thrown an exception: " + e.getMessage());
    }
  }

  /** Verifies: directives case insensitivity. */
  @Test
  public void directiveCaseInsensitivity() {
    final String robotsTxtBodyUpper = "USER-AGENT: FooBot\n" + "ALLOW: /x/\n" + "DISALLOW: /\n";
    final String robotsTxtBodyLower = "user-agent: FooBot\n" + "allow: /x/\n" + "disallow: /\n";
    final String robotsTxtBodyRandom = "uSeR-aGeNt: FooBot\n" + "AlLoW: /x/\n" + "dIsAlLoW: /\n";

    final String urlAllowed = "http://foo.bar/x/y";
    final String urlDisallowed = "http://foo.bar/a/b";

    try {
      final RobotsMatcher matcherUpper = parse(robotsTxtBodyUpper);
      assertTrue(matcherUpper.singleAgentAllowedByRobots("FooBot", urlAllowed));
      assertFalse(matcherUpper.singleAgentAllowedByRobots("FooBot", urlDisallowed));

      final RobotsMatcher matcherLower = parse(robotsTxtBodyLower);
      assertTrue(matcherLower.singleAgentAllowedByRobots("FooBot", urlAllowed));
      assertFalse(matcherLower.singleAgentAllowedByRobots("FooBot", urlDisallowed));

      final RobotsMatcher matcherRandom = parse(robotsTxtBodyRandom);
      assertTrue(matcherRandom.singleAgentAllowedByRobots("FooBot", urlAllowed));
      assertFalse(matcherRandom.singleAgentAllowedByRobots("FooBot", urlDisallowed));
    } catch (final MatchException e) {
      fail("Matcher has thrown an exception: " + e.getMessage());
    }
  }

  /** Verifies: user agent case insensitivity, user agent names convention compliance. */
  @Test
  public void userAgentCaseInsensitivity() {
    final String robotsTxtBodyUpper = "user-agent: FOO BAR\n" + "allow: /x/\n" + "disallow: /\n";
    final String robotsTxtBodyLower = "user-agent: foo bar\n" + "allow: /x/\n" + "disallow: /\n";
    final String robotsTxtBodyRandom = "user-agent: FoO bAr\n" + "allow: /x/\n" + "disallow: /\n";

    final String urlAllowed = "http://foo.bar/x/y";
    final String urlDisallowed = "http://foo.bar/a/b";

    try {
      final RobotsMatcher matcherUpper = parse(robotsTxtBodyUpper);
      assertTrue(matcherUpper.singleAgentAllowedByRobots("Foo", urlAllowed));
      assertTrue(matcherUpper.singleAgentAllowedByRobots("foo", urlAllowed));
      assertFalse(matcherUpper.singleAgentAllowedByRobots("Foo", urlDisallowed));
      assertFalse(matcherUpper.singleAgentAllowedByRobots("foo", urlDisallowed));

      final RobotsMatcher matcherLower = parse(robotsTxtBodyLower);
      assertTrue(matcherLower.singleAgentAllowedByRobots("Foo", urlAllowed));
      assertTrue(matcherLower.singleAgentAllowedByRobots("foo", urlAllowed));
      assertFalse(matcherLower.singleAgentAllowedByRobots("Foo", urlDisallowed));
      assertFalse(matcherLower.singleAgentAllowedByRobots("foo", urlDisallowed));

      final RobotsMatcher matcherRandom = parse(robotsTxtBodyRandom);
      assertTrue(matcherRandom.singleAgentAllowedByRobots("Foo", urlAllowed));
      assertTrue(matcherRandom.singleAgentAllowedByRobots("foo", urlAllowed));
      assertFalse(matcherRandom.singleAgentAllowedByRobots("Foo", urlDisallowed));
      assertFalse(matcherRandom.singleAgentAllowedByRobots("foo", urlDisallowed));
    } catch (final MatchException e) {
      fail("Matcher has thrown an exception: " + e.getMessage());
    }
  }

  /** Validates: global rules. */
  @Test
  public void globalGroups() {
    final String robotsTxtBodyEmpty = "";
    final String robotsTxtBodyGlobal =
        "user-agent: *\n" + "allow: /\n" + "user-agent: FooBot\n" + "disallow: /\n";
    final String robotsTxtBodySpecific =
        "user-agent: FooBot\n"
            + "allow: /\n"
            + "user-agent: BarBot\n"
            + "disallow: /\n"
            + "user-agent: BazBot\n"
            + "disallow: /\n";

    final String url = "http://foo.bar/x/y";

    try {
      final RobotsMatcher matcherEmpty = parse(robotsTxtBodyEmpty);
      assertTrue(matcherEmpty.singleAgentAllowedByRobots("FooBot", url));

      final RobotsMatcher matcherGlobal = parse(robotsTxtBodyGlobal);
      assertFalse(matcherGlobal.singleAgentAllowedByRobots("FooBot", url));
      assertTrue(matcherGlobal.singleAgentAllowedByRobots("BarBot", url));

      final RobotsMatcher matcherSpecific = parse(robotsTxtBodySpecific);
      assertTrue(matcherSpecific.singleAgentAllowedByRobots("QuxBot", url));
    } catch (final MatchException e) {
      fail("Matcher has thrown an exception: " + e.getMessage());
    }
  }

  /** Verifies: case sensitivity of URIs. */
  @Test
  public void uriCaseSensitivity() {
    final String robotsTxtBodyUpper = "user-agent: FooBot\n" + "disallow: /X/\n";
    final String robotsTxtBodyLower = "user-agent: FooBot\n" + "disallow: /x/\n";

    final String url = "http://foo.bar/x/y";

    try {
      final RobotsMatcher matcherUpper = parse(robotsTxtBodyUpper);
      assertTrue(matcherUpper.singleAgentAllowedByRobots("FooBot", url));

      final RobotsMatcher matcherLower = parse(robotsTxtBodyLower);
      assertFalse(matcherLower.singleAgentAllowedByRobots("FooBot", url));
    } catch (final MatchException e) {
      fail("Matcher has thrown an exception: " + e.getMessage());
    }
  }

  @Test
  public void longestMatch() {
    final String url = "http://foo.bar/x/page.html";

    {
      final String robotsTxtBody =
          "user-agent: FooBot\n" + "disallow: /x/page.html\n" + "allow: /x/\n";

      try {
        final RobotsMatcher matcher = parse(robotsTxtBody);
        assertFalse(matcher.singleAgentAllowedByRobots("FooBot", url));
      } catch (final MatchException e) {
        fail("Matcher has thrown an exception: " + e.getMessage());
      }
    }
    {
      final String robotsTxtBody =
          "user-agent: FooBot\n" + "allow: /x/page.html\n" + "disallow: /x/\n";

      try {
        final RobotsMatcher matcher = parse(robotsTxtBody);
        assertTrue(matcher.singleAgentAllowedByRobots("FooBot", url));
        assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/x/"));
      } catch (final MatchException e) {
        fail("Matcher has thrown an exception: " + e.getMessage());
      }
    }
    {
      final String robotsTxtBody = "user-agent: FooBot\n" + "disallow: \n" + "allow: \n";

      try {
        final RobotsMatcher matcher = parse(robotsTxtBody);
        assertTrue(matcher.singleAgentAllowedByRobots("FooBot", url));
      } catch (final MatchException e) {
        fail("Matcher has thrown an exception: " + e.getMessage());
      }
    }
    {
      final String robotsTxtBody = "user-agent: FooBot\n" + "disallow: /\n" + "allow: /\n";

      try {
        final RobotsMatcher matcher = parse(robotsTxtBody);
        assertTrue(matcher.singleAgentAllowedByRobots("FooBot", url));
      } catch (final MatchException e) {
        fail("Matcher has thrown an exception: " + e.getMessage());
      }
    }
    {
      final String robotsTxtBody = "user-agent: FooBot\n" + "disallow: /x\n" + "allow: /x/\n";

      final String url0 = "http://foo.bar/x";
      final String url1 = "http://foo.bar/x/";

      try {
        final RobotsMatcher matcher = parse(robotsTxtBody);
        assertFalse(matcher.singleAgentAllowedByRobots("FooBot", url0));
        assertTrue(matcher.singleAgentAllowedByRobots("FooBot", url1));
      } catch (final MatchException e) {
        fail("Matcher has thrown an exception: " + e.getMessage());
      }
    }
    {
      final String robotsTxtBody =
          "user-agent: FooBot\n" + "disallow: /x/page.html\n" + "allow: /x/page.html\n";

      try {
        final RobotsMatcher matcher = parse(robotsTxtBody);
        assertTrue(matcher.singleAgentAllowedByRobots("FooBot", url));
      } catch (final MatchException e) {
        fail("Matcher has thrown an exception: " + e.getMessage());
      }
    }
    {
      final String robotsTxtBody =
          "user-agent: FooBot\n" + "allow: /page\n" + "disallow: /*.html\n";

      try {
        final RobotsMatcher matcher = parse(robotsTxtBody);
        assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/page.html"));
        assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/page"));
      } catch (final MatchException e) {
        fail("Matcher has thrown an exception: " + e.getMessage());
      }
    }
    {
      final String robotsTxtBody =
          "user-agent: FooBot\n" + "allow: /x/page.\n" + "disallow: /*.html\n";

      try {
        final RobotsMatcher matcher = parse(robotsTxtBody);
        assertTrue(matcher.singleAgentAllowedByRobots("FooBot", url));
        assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/x/y.html"));
      } catch (final MatchException e) {
        fail("Matcher has thrown an exception: " + e.getMessage());
      }
    }
    {
      final String robotsTxtBody =
          "User-agent: *\n" + "Disallow: /x/\n" + "User-agent: FooBot\n" + "Disallow: /y/\n";

      try {
        final RobotsMatcher matcher = parse(robotsTxtBody);
        assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/x/page"));
        assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/y/page"));
      } catch (final MatchException e) {
        fail("Matcher has thrown an exception: " + e.getMessage());
      }
    }
  }

  /** Verifies: valid parsing of special characters ('*', '$', '#') */
  @Test
  public void specialCharacters() {
    {
      final String robotsTxtBody =
          "User-agent: FooBot\n" + "Disallow: /foo/bar/quz\n" + "Allow: /foo/*/qux\n";

      try {
        final RobotsMatcher matcher = parse(robotsTxtBody);
        assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo/bar/quz"));
        assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo/quz"));
        assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo//quz"));
        assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo/bax/quz"));
      } catch (final MatchException e) {
        fail("Matcher has thrown an exception: " + e.getMessage());
      }
    }
    {
      final String robotsTxtBody =
          "User-agent: FooBot\n" + "Disallow: /foo/bar$\n" + "Allow: /foo/bar/qux\n";

      try {
        final RobotsMatcher matcher = parse(robotsTxtBody);
        assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo/bar"));
        assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo/bar/qux"));
        assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo/bar/"));
        assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo/bar/baz"));
      } catch (final MatchException e) {
        fail("Matcher has thrown an exception: " + e.getMessage());
      }
    }
    {
      final String robotsTxtBody =
          "User-agent: FooBot\n" + "# Disallow: /\n" + "Disallow: /foo/quz#qux\n" + "Allow: /\n";

      try {
        final RobotsMatcher matcher = parse(robotsTxtBody);
        assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo/bar"));
        assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo/quz"));
      } catch (final MatchException e) {
        fail("Matcher has thrown an exception: " + e.getMessage());
      }
    }
  }
}
