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

import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine;

/**
 * Console application for parsing robots.txt and matching URLs against it.
 *
 * @see Parser
 * @see Matcher
 */
@CommandLine.Command(
    name = "robotsParser",
    description =
        "Parses and matches given agents against given robots.txt to determine "
            + "whether any agent is allowed to visit given URL.",
    exitCodeOnExecutionException = 2,
    exitCodeOnInvalidInput = 3
)
public class RobotsParserApp implements Callable<Integer> {
  public RobotsParserApp() {}

  public static void main(final String[] args) {
    final int exitCode = new CommandLine(new RobotsParserApp()).execute(args);
    System.exit(exitCode);
  }

  /** robots.txt contents. */
  @CommandLine.Option(
      names = {"-r", "--robotstxt"},
      required = true)
  private String robotsTxtContents;

  /** Interested user-agents. */
  @CommandLine.Option(
      names = {"-a", "--agents"},
      required = true)
  private List<String> agents;

  /** Target URL to match. */
  @CommandLine.Option(
      names = {"-u", "--url"},
      required = true)
  private String url;

  /**
   * Parses given robots.txt contents and performs matching process.
   *
   * @return {@code 0} if any of user-agents is allowed to crawl given URL and {@code 1} otherwise.
   * @throws MatchException if exception occurred during matching process.
   */
  @Override
  public Integer call() throws MatchException {
    final Parser parser = new RobotsParser(new RobotsParseHandler());
    final RobotsMatcher matcher = (RobotsMatcher) parser.parse(robotsTxtContents);

    final boolean parseResult = matcher.allowedByRobots(agents, url);

    if (parseResult) {
      System.out.println("ALLOWED");
      return 0;
    } else {
      System.out.println("DISALLOWED");
      return 1;
    }
  }
}
