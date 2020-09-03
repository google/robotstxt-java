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

import com.google.common.flogger.FluentLogger;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
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
    exitCodeOnInvalidInput = 3)
public class RobotsParserApp implements Callable<Integer> {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  public RobotsParserApp() {}

  public static void main(final String[] args) {
    final int exitCode = new CommandLine(new RobotsParserApp()).execute(args);
    System.exit(exitCode);
  }

  /** robots.txt file path. */
  @CommandLine.Option(names = {"-f", "--file"})
  private String robotsTxtPath;

  /** Interested user-agents. */
  @CommandLine.Option(
      names = {"-a", "--agent"},
      required = true)
  private List<String> agents;

  /** Target URL to match. */
  @CommandLine.Option(
      names = {"-u", "--url"},
      required = true)
  private String url;

  private byte[] readRobotsTxt() throws ParseException {
    try {
      if (Objects.isNull(robotsTxtPath)) {
        // Reading from stdin
        return ByteStreams.toByteArray(System.in);
      } else {
        // Reading from file
        return Files.readAllBytes(Path.of(robotsTxtPath));
      }
    } catch (final UncheckedIOException | IOException | InvalidPathException e) {
      throw new ParseException("Failed to read robots.txt file.", e);
    }
  }

  private static void logError(final Exception e) {
    System.out.println("ERROR: " + e.getMessage());
    logger.atInfo().withCause(e).log("Stack trace:");
  }

  /**
   * Parses given robots.txt file and performs matching process.
   *
   * @return {@code 0} if any of user-agents is allowed to crawl given URL and {@code 1} otherwise.
   */
  @Override
  public Integer call() {
    final byte[] robotsTxtContents;
    try {
      robotsTxtContents = readRobotsTxt();
    } catch (final ParseException e) {
      logError(e);
      return 2;
    }

    final Parser parser = new RobotsParser(new RobotsParseHandler());
    final RobotsMatcher matcher = (RobotsMatcher) parser.parse(robotsTxtContents);

    final boolean parseResult;
    parseResult = matcher.allowedByRobots(agents, url);

    if (parseResult) {
      System.out.println("ALLOWED");
      return 0;
    } else {
      System.out.println("DISALLOWED");
      return 1;
    }
  }
}
