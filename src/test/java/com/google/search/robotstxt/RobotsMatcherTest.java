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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.Test;

/** Unit tests validating matching behavior. */
public class RobotsMatcherTest {
  private static Matcher parse(final String robotsTxtBody) {
    final Parser parser = new RobotsParser(new RobotsParseHandler());
    return parser.parse(robotsTxtBody.getBytes(StandardCharsets.UTF_8));
  }

  /** Verifies: parsing an matching robots.txt containing single group */
  @Test
  public void testSingleGroup() {
    final String robotsTxtBodyCorrect = "user-agent: FooBot\n" + "disallow: /\n";
    final String robotsTxtBodyIncorrect = "foo: FooBot\n" + "bar: /\n";

    final String url = "http://foo.bar/x/y";

    final Matcher matcherCorrect = parse(robotsTxtBodyCorrect);
    assertFalse(matcherCorrect.singleAgentAllowedByRobots("FooBot", url));

    final Matcher matcherIncorrect = parse(robotsTxtBodyIncorrect);
    assertTrue(matcherIncorrect.singleAgentAllowedByRobots("FooBot", url));
  }

  /**
   * Verifies: parsing an matching robots.txt containing multiple groups, invalid directives
   * ignorance.
   */
  @Test
  public void testMultipleGroups() {
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

    final Matcher matcher = parse(robotsTxtBody);

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
  }

  /** Verifies: directives case insensitivity. */
  @Test
  public void testDirectiveCaseInsensitivity() {
    final String robotsTxtBodyUpper = "USER-AGENT: FooBot\n" + "ALLOW: /x/\n" + "DISALLOW: /\n";
    final String robotsTxtBodyLower = "user-agent: FooBot\n" + "allow: /x/\n" + "disallow: /\n";
    final String robotsTxtBodyRandom = "uSeR-aGeNt: FooBot\n" + "AlLoW: /x/\n" + "dIsAlLoW: /\n";

    final String urlAllowed = "http://foo.bar/x/y";
    final String urlDisallowed = "http://foo.bar/a/b";

    final Matcher matcherUpper = parse(robotsTxtBodyUpper);
    assertTrue(matcherUpper.singleAgentAllowedByRobots("FooBot", urlAllowed));
    assertFalse(matcherUpper.singleAgentAllowedByRobots("FooBot", urlDisallowed));

    final Matcher matcherLower = parse(robotsTxtBodyLower);
    assertTrue(matcherLower.singleAgentAllowedByRobots("FooBot", urlAllowed));
    assertFalse(matcherLower.singleAgentAllowedByRobots("FooBot", urlDisallowed));

    final Matcher matcherRandom = parse(robotsTxtBodyRandom);
    assertTrue(matcherRandom.singleAgentAllowedByRobots("FooBot", urlAllowed));
    assertFalse(matcherRandom.singleAgentAllowedByRobots("FooBot", urlDisallowed));
  }

  /** Verifies: user agent case insensitivity, user agent names convention compliance. */
  @Test
  public void testUserAgentCaseInsensitivity() {
    final String robotsTxtBodyUpper = "user-agent: FOO BAR\n" + "allow: /x/\n" + "disallow: /\n";
    final String robotsTxtBodyLower = "user-agent: foo bar\n" + "allow: /x/\n" + "disallow: /\n";
    final String robotsTxtBodyRandom = "user-agent: FoO bAr\n" + "allow: /x/\n" + "disallow: /\n";

    final String urlAllowed = "http://foo.bar/x/y";
    final String urlDisallowed = "http://foo.bar/a/b";

    final Matcher matcherUpper = parse(robotsTxtBodyUpper);
    assertTrue(matcherUpper.singleAgentAllowedByRobots("Foo", urlAllowed));
    assertTrue(matcherUpper.singleAgentAllowedByRobots("foo", urlAllowed));
    assertFalse(matcherUpper.singleAgentAllowedByRobots("Foo", urlDisallowed));
    assertFalse(matcherUpper.singleAgentAllowedByRobots("foo", urlDisallowed));

    final Matcher matcherLower = parse(robotsTxtBodyLower);
    assertTrue(matcherLower.singleAgentAllowedByRobots("Foo", urlAllowed));
    assertTrue(matcherLower.singleAgentAllowedByRobots("foo", urlAllowed));
    assertFalse(matcherLower.singleAgentAllowedByRobots("Foo", urlDisallowed));
    assertFalse(matcherLower.singleAgentAllowedByRobots("foo", urlDisallowed));

    final Matcher matcherRandom = parse(robotsTxtBodyRandom);
    assertTrue(matcherRandom.singleAgentAllowedByRobots("Foo", urlAllowed));
    assertTrue(matcherRandom.singleAgentAllowedByRobots("foo", urlAllowed));
    assertFalse(matcherRandom.singleAgentAllowedByRobots("Foo", urlDisallowed));
    assertFalse(matcherRandom.singleAgentAllowedByRobots("foo", urlDisallowed));
  }

  /** Verifies: global rules. */
  @Test
  public void testGlobalGroups() {
    final String robotsTxtBodyEmpty = "";
    final String robotsTxtBodyGlobal =
        "user-agent: *\n" + "disallow: /x\n" + "user-agent: FooBot\n" + "allow: /x/y\n";
    final String robotsTxtBodySpecific =
        "user-agent: FooBot\n"
            + "allow: /\n"
            + "user-agent: BarBot\n"
            + "disallow: /\n"
            + "user-agent: BazBot\n"
            + "disallow: /\n";

    final String url = "http://foo.bar/x/y";

    final Matcher matcherEmpty = parse(robotsTxtBodyEmpty);
    assertTrue(matcherEmpty.singleAgentAllowedByRobots("FooBot", url));

    final Matcher matcherGlobal = parse(robotsTxtBodyGlobal);
    assertTrue(matcherGlobal.singleAgentAllowedByRobots("FooBot", url));
    assertFalse(matcherGlobal.singleAgentAllowedByRobots("BarBot", url));

    final Matcher matcherSpecific = parse(robotsTxtBodySpecific);
    assertTrue(matcherSpecific.singleAgentAllowedByRobots("QuxBot", url));
  }

  /**
   * [Google-specific] Verifies: any user-agent with prefix "* " is considered as global wildcard.
   */
  @Test
  public void testGlobalGroupsPrefix() {
    final String robotsTxtBody =
        "user-agent: * baz\n" + "disallow: /x\n" + "user-agent: FooBot\n" + "allow: /x/y\n";

    final String url = "http://foo.bar/x/y";

    final Matcher matcher = parse(robotsTxtBody);
    assertTrue(matcher.singleAgentAllowedByRobots("FooBot", url));
    assertFalse(matcher.singleAgentAllowedByRobots("BarBot", url));
  }

  /** Verifies: case sensitivity of URIs. */
  @Test
  public void testUriCaseSensitivity() {
    final String robotsTxtBodyUpper = "user-agent: FooBot\n" + "disallow: /X/\n";
    final String robotsTxtBodyLower = "user-agent: FooBot\n" + "disallow: /x/\n";

    final String url = "http://foo.bar/x/y";

    final Matcher matcherUpper = parse(robotsTxtBodyUpper);
    assertTrue(matcherUpper.singleAgentAllowedByRobots("FooBot", url));

    final Matcher matcherLower = parse(robotsTxtBodyLower);
    assertFalse(matcherLower.singleAgentAllowedByRobots("FooBot", url));
  }

  /** Verifies: longest match strategy. */
  @Test
  public void testLongestMatch() {
    final String url = "http://foo.bar/x/page.html";

    {
      final String robotsTxtBody =
          "user-agent: FooBot\n" + "disallow: /x/page.html\n" + "allow: /x/\n";

      final Matcher matcher = parse(robotsTxtBody);
      assertFalse(matcher.singleAgentAllowedByRobots("FooBot", url));
    }
    {
      final String robotsTxtBody =
          "user-agent: FooBot\n" + "allow: /x/page.html\n" + "disallow: /x/\n";

      final Matcher matcher = parse(robotsTxtBody);
      assertTrue(matcher.singleAgentAllowedByRobots("FooBot", url));
      assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/x/"));
    }
    {
      final String robotsTxtBody = "user-agent: FooBot\n" + "disallow: \n" + "allow: \n";

      final Matcher matcher = parse(robotsTxtBody);
      assertTrue(matcher.singleAgentAllowedByRobots("FooBot", url));
    }
    {
      final String robotsTxtBody = "user-agent: FooBot\n" + "disallow: /\n" + "allow: /\n";

      final Matcher matcher = parse(robotsTxtBody);
      assertTrue(matcher.singleAgentAllowedByRobots("FooBot", url));
    }
    {
      final String robotsTxtBody = "user-agent: FooBot\n" + "disallow: /x\n" + "allow: /x/\n";

      final String url0 = "http://foo.bar/x";
      final String url1 = "http://foo.bar/x/";

      final Matcher matcher = parse(robotsTxtBody);
      assertFalse(matcher.singleAgentAllowedByRobots("FooBot", url0));
      assertTrue(matcher.singleAgentAllowedByRobots("FooBot", url1));
    }
    {
      final String robotsTxtBody =
          "user-agent: FooBot\n" + "disallow: /x/page.html\n" + "allow: /x/page.html\n";

      final Matcher matcher = parse(robotsTxtBody);
      assertTrue(matcher.singleAgentAllowedByRobots("FooBot", url));
    }
    {
      final String robotsTxtBody =
          "user-agent: FooBot\n" + "allow: /page\n" + "disallow: /*.html\n";

      final Matcher matcher = parse(robotsTxtBody);
      assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/page.html"));
      assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/page"));
    }
    {
      final String robotsTxtBody =
          "user-agent: FooBot\n" + "allow: /x/page.\n" + "disallow: /*.html\n";

      final Matcher matcher = parse(robotsTxtBody);
      assertTrue(matcher.singleAgentAllowedByRobots("FooBot", url));
      assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/x/y.html"));
    }
    {
      final String robotsTxtBody =
          "User-agent: *\n" + "Disallow: /x/\n" + "User-agent: FooBot\n" + "Disallow: /y/\n";

      final Matcher matcher = parse(robotsTxtBody);
      assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/x/page"));
      assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/y/page"));
    }
  }

  /** Verifies: valid parsing of special characters ('*', '$', '#') */
  @Test
  public void testSpecialCharacters() {
    {
      final String robotsTxtBody =
          "User-agent: FooBot\n" + "Disallow: /foo/bar/quz\n" + "Allow: /foo/*/qux\n";

      final Matcher matcher = parse(robotsTxtBody);
      assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo/bar/quz"));
      assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo/quz"));
      assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo//quz"));
      assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo/bax/quz"));
    }
    {
      final String robotsTxtBody =
          "User-agent: FooBot\n" + "Disallow: /foo/bar$\n" + "Allow: /foo/bar/qux\n";

      final Matcher matcher = parse(robotsTxtBody);
      assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo/bar"));
      assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo/bar/qux"));
      assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo/bar/"));
      assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo/bar/baz"));
    }
    {
      final String robotsTxtBody =
          "User-agent: FooBot\n" + "# Disallow: /\n" + "Disallow: /foo/quz#qux\n" + "Allow: /\n";

      final Matcher matcher = parse(robotsTxtBody);
      assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo/bar"));
      assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/foo/quz"));
    }
  }

  /**
   * [Google-specific] Verifies: {@code /index.htm} or {@code /index.html} should be normalised to
   * {@code /}.
   */
  @Test
  public void testIndexNormalisation() {
    final String robotsTxtBody =
        "user-agent: FooBot\n"
            + "disallow: /\n"
            + "allow: /index.htm\n"
            + "allow: /index.html\n"
            + "allow: /x\n"
            + "disallow: /x/index.htm\n"
            + "disallow: /x/index.html\n";

    final String[] urls = {
      "http://foo.bar/",
      "http://foo.bar/index.htm",
      "http://foo.bar/index.html",
      "http://foo.bar/x/",
      "http://foo.bar/x/index.htm",
      "http://foo.bar/x/index.html"
    };

    final Matcher matcher = parse(robotsTxtBody);
    assertTrue(matcher.singleAgentAllowedByRobots("FooBot", urls[0]));
    assertTrue(matcher.singleAgentAllowedByRobots("FooBot", urls[1]));
    assertTrue(matcher.singleAgentAllowedByRobots("FooBot", urls[2]));
    assertTrue(matcher.singleAgentAllowedByRobots("FooBot", urls[3]));
    assertFalse(matcher.singleAgentAllowedByRobots("FooBot", urls[4]));
    assertFalse(matcher.singleAgentAllowedByRobots("FooBot", urls[5]));
  }

  /** [Google-specific] Verifies: common typos in {@code DISALLOW} key should be fixed. */
  @Test
  public void testTyposFixes() {
    final String robotsTxtBody =
        "user-agent: FooBot\n"
            + "disallow: /a/\n"
            + "dissallow: /b/\n"
            + "dissalow: /c/\n"
            + "disalow: /d/\n"
            + "diasllow: /e/\n"
            + "disallaw: /f/\n";

    final Matcher matcher = parse(robotsTxtBody);
    assertTrue(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/index.html"));
    assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/a/"));
    assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/b/"));
    assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/c/"));
    assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/d/"));
    assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/e/"));
    assertFalse(matcher.singleAgentAllowedByRobots("FooBot", "http://foo.bar/f/"));
  }
}
