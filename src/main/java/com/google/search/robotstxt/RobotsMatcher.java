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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Class implementing matching logic based on directives priorities those calculation is delegated
 * to a {@link MatchingStrategy} class.
 */
public class RobotsMatcher implements Matcher {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  /** Class containing current match priorities */
  private static class Match {
    /** Priority based on agent-specific rules */
    private int prioritySpecific = 0;
    /** Priority based on global wildcard (*) rules */
    private int priorityGlobal = 0;

    void updateSpecific(final int priority) {
      prioritySpecific = Math.max(prioritySpecific, priority);
    }

    void updateGlobal(final int priority) {
      priorityGlobal = Math.max(priorityGlobal, priority);
    }

    public int getPrioritySpecific() {
      return prioritySpecific;
    }

    public int getPriorityGlobal() {
      return priorityGlobal;
    }

    public void resetGlobal() {
      priorityGlobal = 0;
    }
  }

  private final RobotsContents robotsContents;
  private final MatchingStrategy matchingStrategy = new RobotsLongestMatchStrategy();

  public RobotsMatcher(final RobotsContents robotsContents) {
    this.robotsContents = robotsContents;
  }

  /** Used to extract contents for testing purposes. */
  RobotsContents getRobotsContents() {
    return robotsContents;
  }

  private static String getPath(final String url) throws MatchException {
    try {
      String path = new URL(url).getPath();

      // Google-specific optimization: 'index.htm' and 'index.html' are normalized to '/'.
      final int slashPos = path.lastIndexOf('/');

      if (slashPos != -1) {
        final String fileName = path.substring(slashPos + 1);
        if ("index.htm".equals(fileName) || "index.html".equals(fileName)) {
          final String newPath = path.substring(0, slashPos + 1);

          logger.atInfo().log("Omitted index page (\"%s\" -> \"%s\")", path, newPath);

          path = newPath;
        }
      }

      return path;
    } catch (final MalformedURLException e) {
      throw new MatchException("Malformed URL was given.", e);
    }
  }

  /**
   * Computes {@link Match} priorities for ALLOW and DISALLOW verdicts. Rules are considered
   * effective if at least one user agent is listed in "user-agent" directives or applied globally
   * (if global rules are not ignored).
   *
   * @param userAgents list of interested user agents
   * @param path target path
   * @param ignoreGlobal global rules will not be considered if set to {@code true}
   * @return pair of {@link Match} representing ALLOW and DISALLOW priorities respectively
   */
  private Map.Entry<Match, Match> computeMatchPriorities(
      final List<String> userAgents, final String path, final boolean ignoreGlobal) {
    final Match allow = new Match();
    final Match disallow = new Match();
    boolean foundSpecificGroup = false;

    for (RobotsContents.Group group : robotsContents.getGroups()) {
      final boolean isSpecificGroup =
          userAgents.stream()
              .anyMatch(
                  userAgent ->
                      group.getUserAgents().stream().anyMatch(userAgent::equalsIgnoreCase));
      foundSpecificGroup |= isSpecificGroup;
      if (!isSpecificGroup && (ignoreGlobal || !group.isGlobal())) {
        continue;
      }

      for (RobotsContents.Group.Rule rule : group.getRules()) {
        switch (rule.getDirectiveType()) {
          case ALLOW:
            {
              final int priority =
                  matchingStrategy.matchAllowPriority(path, rule.getDirectiveValue());
              if (isSpecificGroup) {
                allow.updateSpecific(priority);
              }
              if (!ignoreGlobal && group.isGlobal()) {
                allow.updateGlobal(priority);
              }
              break;
            }
          case DISALLOW:
            {
              final int priority =
                  matchingStrategy.matchDisallowPriority(path, rule.getDirectiveValue());
              if (isSpecificGroup) {
                disallow.updateSpecific(priority);
              }
              if (!ignoreGlobal && group.isGlobal()) {
                disallow.updateGlobal(priority);
              }
              break;
            }
          case SITEMAP:
          case UNKNOWN:
          case USER_AGENT:
            break;
        }
      }
    }

    // If there is at least one group specific for current agents, global groups should be
    // disregarded.
    if (foundSpecificGroup) {
      allow.resetGlobal();
      disallow.resetGlobal();
    }

    return Map.entry(allow, disallow);
  }

  private Map.Entry<Match, Match> computeMatchPriorities(
      final List<String> userAgents, final String path) {
    return computeMatchPriorities(userAgents, path, false);
  }

  /**
   * Return {@code true} iff verdict must be ALLOW based on ALLOW and DISALLOW priorities.
   *
   * @param allow ALLOW priorities
   * @param disallow DISALLOW priorities
   * @return match verdict
   */
  private static boolean allowVerdict(final Match allow, final Match disallow) {
    if (allow.getPrioritySpecific() > 0 || disallow.getPrioritySpecific() > 0) {
      return allow.getPrioritySpecific() >= disallow.getPrioritySpecific();
    }

    if (allow.getPriorityGlobal() > 0 || disallow.getPriorityGlobal() > 0) {
      return allow.getPriorityGlobal() >= disallow.getPriorityGlobal();
    }

    return true;
  }

  @Override
  public boolean allowedByRobots(final List<String> userAgents, final String url)
      throws MatchException {
    final String path = getPath(url);
    Map.Entry<Match, Match> matches = computeMatchPriorities(userAgents, path);
    return allowVerdict(matches.getKey(), matches.getValue());
  }

  @Override
  public boolean singleAgentAllowedByRobots(final String userAgent, final String url)
      throws MatchException {
    return allowedByRobots(Collections.singletonList(userAgent), url);
  }

  @Override
  public boolean ignoreGlobalAllowedByRobots(final List<String> userAgents, final String url)
      throws MatchException {
    final String path = getPath(url);
    Map.Entry<Match, Match> matches = computeMatchPriorities(userAgents, path, true);
    return allowVerdict(matches.getKey(), matches.getValue());
  }
}
