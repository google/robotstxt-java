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

/** Implementation of parsing strategy used in robots.txt parsing. */
public class RobotsParseHandler implements ParseHandler {
  protected RobotsContents robotsContents;
  private RobotsContents.Group currentGroup;

  @Override
  public void handleStart() {
    robotsContents = new RobotsContents();
    currentGroup = new RobotsContents.Group();
  }

  private void flushCompleteGroup(boolean createNew) {
    if (currentGroup.getRules().size() > 0) {
      robotsContents.addGroup(currentGroup);
      if (createNew) {
        currentGroup = new RobotsContents.Group();
      }
    }
  }

  @Override
  public void handleEnd() {
    flushCompleteGroup(false);
  }

  private void handleUserAgent(final String value) {
    flushCompleteGroup(true);
    currentGroup.addUserAgent(value);
  }

  @Override
  public void handleDirective(
      final Parser.DirectiveType directiveType, final String directiveValue) {
    switch (directiveType) {
      case USER_AGENT:
        {
          handleUserAgent(directiveValue);
          break;
        }
      case ALLOW:
      case DISALLOW:
        {
          if (currentGroup.isGlobal() || currentGroup.getUserAgents().size() > 0) {
            currentGroup.addRule(directiveType, directiveValue);
          }
          break;
        }
      case SITEMAP:
      case UNKNOWN:
        {
          // Ignored.
          break;
        }
    }
  }

  @Override
  public Matcher compute() {
    return new RobotsMatcher(robotsContents);
  }
}
