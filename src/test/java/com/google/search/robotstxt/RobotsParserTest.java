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

import static com.google.common.truth.Truth.assertThat;

import com.google.search.robotstxt.fakes.FakeMatcher;
import com.google.search.robotstxt.fakes.FakeParseHandler;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

/**
 * Unit tests validating parsing behavior.
 *
 * @see RobotsParser
 */
public class RobotsParserTest {
  /**
   * Parses given robots.txt contents via {@link RobotsParser} and compares the result with an
   * expected one.
   *
   * @param robotsTxtBody Contents of robots.txt file
   * @param expectedContents Expected contents
   */
  private static void parseAndValidate(
      final String robotsTxtBody, final RobotsContents expectedContents) {
    Parser parser = new RobotsParser(new FakeParseHandler());
    FakeMatcher matcher = (FakeMatcher) parser.parse(robotsTxtBody);
    RobotsContents actualContents = matcher.getRobotsContents();

    expectedContents
        .getGroups()
        .forEach(expectedGroup -> assertThat(expectedGroup).isIn(actualContents.getGroups()));
  }

  /** Verifies: rules grouping, rules parsing, invalid directives ignorance. */
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

    final RobotsContents expectedContents =
        new RobotsContents(
            Arrays.asList(
                new RobotsContents.Group(
                    Collections.singletonList("FooBot"),
                    Arrays.asList(
                        new RobotsContents.Group.Rule(Parser.DirectiveType.DISALLOW, "/"),
                        new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/x/"))),
                new RobotsContents.Group(
                    Collections.singletonList("BarBot"),
                    Arrays.asList(
                        new RobotsContents.Group.Rule(Parser.DirectiveType.DISALLOW, "/"),
                        new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/y/"),
                        new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/w/"))),
                new RobotsContents.Group(
                    Arrays.asList("BazBot", "FooBot"),
                    Arrays.asList(
                        new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/z/"),
                        new RobotsContents.Group.Rule(Parser.DirectiveType.DISALLOW, "/")))));

    parseAndValidate(robotsTxtBody, expectedContents);
  }

  /** Verifies: CR character must be treated as EOL, invalid directives ignorance. */
  @Test
  public void testCr() {
    final String robotsTxtBody =
        "user-agent: FooBot\n"
            + "disallow: /\n"
            + "allow: /x/\rallow: /y/\n"
            + "al\r\r\r\r\rdisallow: /z/\n";

    final RobotsContents expectedContents =
        new RobotsContents(
            Collections.singletonList(
                new RobotsContents.Group(
                    Collections.singletonList("FooBot"),
                    Arrays.asList(
                        new RobotsContents.Group.Rule(Parser.DirectiveType.DISALLOW, "/"),
                        new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/x/"),
                        new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/y/"),
                        new RobotsContents.Group.Rule(Parser.DirectiveType.DISALLOW, "/z/")))));

    parseAndValidate(robotsTxtBody, expectedContents);
  }

  /** Verifies: CL RF must be treated as EOL. */
  @Test
  public void testCrLf() {
    final String robotsTxtBody =
        "allow: /foo/bar/\r\n"
            + "\r\n"
            + "user-agent: FooBot\r\n"
            + "disallow: /\r\n"
            + "allow: /x/\r\n"
            + "user-agent: BarBot\r\n"
            + "disallow: /\r\n"
            + "allow: /y/\r\n"
            + "\r\n";

    final RobotsContents expectedContents =
        new RobotsContents(
            Arrays.asList(
                new RobotsContents.Group(
                    Collections.singletonList("FooBot"),
                    Arrays.asList(
                        new RobotsContents.Group.Rule(Parser.DirectiveType.DISALLOW, "/"),
                        new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/x/"))),
                new RobotsContents.Group(
                    Collections.singletonList("BarBot"),
                    Arrays.asList(
                        new RobotsContents.Group.Rule(Parser.DirectiveType.DISALLOW, "/"),
                        new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/y/")))));

    parseAndValidate(robotsTxtBody, expectedContents);
  }

  /** Verifies: surrounding whitespace characters (LF, CR) ignorance. */
  @Test
  public void testWhitespaces() {
    final String robotsTxtBody =
        "user-agent \t: \tFooBot\n"
            + "disallow  : /  \n"
            + "  allow:  /x/\n"
            + "    \n"
            + " \t \t \n"
            + "user-agent:BarBot\n"
            + "\t \t disallow\t \t :\t \t /\t \t \n"
            + "\t\tallow\t\t:\t\t/y/\t\t\n"
            + "\n";

    final RobotsContents expectedContents =
        new RobotsContents(
            Arrays.asList(
                new RobotsContents.Group(
                    Collections.singletonList("FooBot"),
                    Arrays.asList(
                        new RobotsContents.Group.Rule(Parser.DirectiveType.DISALLOW, "/"),
                        new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/x/"))),
                new RobotsContents.Group(
                    Collections.singletonList("BarBot"),
                    Arrays.asList(
                        new RobotsContents.Group.Rule(Parser.DirectiveType.DISALLOW, "/"),
                        new RobotsContents.Group.Rule(Parser.DirectiveType.ALLOW, "/y/")))));

    parseAndValidate(robotsTxtBody, expectedContents);
  }
}
