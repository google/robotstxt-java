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

import com.google.search.robotstxt.fakes.FakeMatcher;
import com.google.search.robotstxt.fakes.FakeParseHandler;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Unit tests validating parsing behavior.
 *
 * @see RobotsParser
 */
public class RobotsParserTest extends Assert {
  /**
   * Verifies whether given rule groups contains same rules applied to same user-agents.
   *
   * @param expected expected group
   * @param actual group group
   * @return {@code true} iff groups are equivalent.
   */
  private static boolean areEquivalentGroups(final RobotsContents.Group expected,
                                             final RobotsContents.Group actual) {
    return expected.getUserAgents().size() == actual.getUserAgents().size() &&
        expected.getRules().size() == actual.getRules().size() &&
        actual.getUserAgents().containsAll(expected.getUserAgents()) &&
        actual.getRules().containsAll(expected.getRules());
  }

  /**
   * Verifies whether given robots.txt representations contain equivalent rule groups.
   *
   * @param expected expected robots.txt contents
   * @param actual actual robots.txt contents
   */
  private static void validateContents(final RobotsContents expected,
                                       final RobotsContents actual) {
    assertEquals(expected.getGroups().size(), actual.getGroups().size());
    expected.getGroups().forEach(expectedGroup ->
        assertTrue(
            actual.getGroups().stream().anyMatch(
                actualGroup -> areEquivalentGroups(expectedGroup, actualGroup))
        )
    );
  }

  /**
   * Parses given robots.txt contents via {@link RobotsParser} and compares the result with
   * an expected one.
   *
   * @param robotsTxtBody Contents of robots.txt file
   * @param expectedContents Expected contents
   */
  private static void parseAndValidate(final String robotsTxtBody,
                                       final RobotsContents expectedContents) {
    Parser parser = new RobotsParser(new FakeParseHandler());
    FakeMatcher matcher = (FakeMatcher) parser.parse(robotsTxtBody);
    RobotsContents actualContents = matcher.getRobotsContents();

    validateContents(expectedContents, actualContents);
  }

  /**
   * Verifies: rules grouping, rules parsing, invalid directives ignorance.
   */
  @Test
  public void testMultipleGroups() {
    final String robotsTxtBody =
        "allow: /foo/bar/\n" +
        "\n" +
        "user-agent: FooBot\n" +
        "disallow: /\n" +
        "allow: /x/\n" +
        "user-agent: BarBot\n" +
        "disallow: /\n" +
        "allow: /y/\n" +
        "\n" +
        "\n" +
        "allow: /w/\n" +
        "user-agent: BazBot\n" +
        "\n" +
        "user-agent: FooBot\n" +
        "allow: /z/\n" +
        "disallow: /\n";

    final RobotsContents expectedContents = new RobotsContents(
        Arrays.asList(
            new RobotsContents.Group(
                Collections.singletonList("FooBot"),
                Arrays.asList(
                    new RobotsContents.Group.Rule(Parser.DirectiveType.DISALLOW, "/"),
                    new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/x/")
                )
            ),
            new RobotsContents.Group(
                Collections.singletonList("BarBot"),
                Arrays.asList(
                    new RobotsContents.Group.Rule(Parser.DirectiveType.DISALLOW, "/"),
                    new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/y/"),
                    new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/w/")
                )
            ),
            new RobotsContents.Group(
                Arrays.asList("BazBot", "FooBot"),
                Arrays.asList(
                    new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/z/"),
                    new RobotsContents.Group.Rule(Parser.DirectiveType.DISALLOW, "/")
                )
            )
        )
    );

    parseAndValidate(robotsTxtBody, expectedContents);
  }

  /**
   * Verifies: CR character must be treated as EOL, invalid directives ignorance.
   */
  @Test
  public void testCr() {
    final String robotsTxtBody =
        "user-agent: FooBot\n" +
        "disallow: /\n" +
        "allow: /x/\rallow: /y/\n" +
        "al\r\r\r\r\rdisallow: /z/\n";

    final RobotsContents expectedContents = new RobotsContents(
        Collections.singletonList(
            new RobotsContents.Group(
                Collections.singletonList("FooBot"),
                Arrays.asList(
                    new RobotsContents.Group.Rule(Parser.DirectiveType.DISALLOW, "/"),
                    new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/x/"),
                    new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/y/"),
                    new RobotsContents.Group.Rule(Parser.DirectiveType.DISALLOW, "/z/")
                )
            )
        )
    );

    parseAndValidate(robotsTxtBody, expectedContents);
  }

  /**
   * Verifies: CL RF must be treated as EOL.
   */
  @Test
  public void testCrLf() {
    final String robotsTxtBody =
        "allow: /foo/bar/\r\n" +
        "\r\n" +
        "user-agent: FooBot\r\n" +
        "disallow: /\r\n" +
        "allow: /x/\r\n" +
        "user-agent: BarBot\r\n" +
        "disallow: /\r\n" +
        "allow: /y/\r\n" +
        "\r\n";

    final RobotsContents expectedContents = new RobotsContents(
        Arrays.asList(
            new RobotsContents.Group(
                Collections.singletonList("FooBot"),
                Arrays.asList(
                    new RobotsContents.Group.Rule(Parser.DirectiveType.DISALLOW, "/"),
                    new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/x/")
                )
            ),
            new RobotsContents.Group(
                Collections.singletonList("BarBot"),
                Arrays.asList(
                    new RobotsContents.Group.Rule(Parser.DirectiveType.DISALLOW, "/"),
                    new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/y/")
                )
            )
        )
    );

    parseAndValidate(robotsTxtBody, expectedContents);
  }

  /**
   * Verifies: surrounding whitespace characters (0x0A, 0x20) ignorance.
   */
  @Test
  public void testWhitespaces() {
    final String robotsTxtBody =
        "user-agent \t: \tFooBot\n" +
        "disallow  : /  \n" +
        "  allow:  /x/\n" +
        "    \n" +
        " \t \t \n" +
        "user-agent:BarBot\n" +
        "\t \t disallow\t \t :\t \t /\t \t \n" +
        "\t\tallow\t\t:\t\t/y/\t\t\n" +
        "\n";

    final RobotsContents expectedContents = new RobotsContents(
        Arrays.asList(
            new RobotsContents.Group(
                Collections.singletonList("FooBot"),
                Arrays.asList(
                    new RobotsContents.Group.Rule(Parser.DirectiveType.DISALLOW, "/"),
                    new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/x/")
                )
            ),
            new RobotsContents.Group(
                Collections.singletonList("BarBot"),
                Arrays.asList(
                    new RobotsContents.Group.Rule(Parser.DirectiveType.DISALLOW, "/"),
                    new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/y/")
                )
            )
        )
    );

    parseAndValidate(robotsTxtBody, expectedContents);
  }
}
