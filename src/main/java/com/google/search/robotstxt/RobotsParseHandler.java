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
import java.nio.charset.StandardCharsets;

/** Implementation of parsing strategy used in robots.txt parsing. */
public class RobotsParseHandler implements ParseHandler {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  protected RobotsContents robotsContents;
  private RobotsContents.Group currentGroup;
  private boolean foundContent;

  @Override
  public void handleStart() {
    robotsContents = new RobotsContents();
    currentGroup = new RobotsContents.Group();
    foundContent = false;
  }

  private void flushCompleteGroup(boolean createNew) {
    robotsContents.addGroup(currentGroup);
    if (createNew) {
      currentGroup = new RobotsContents.Group();
    }
  }

  @Override
  public void handleEnd() {
    flushCompleteGroup(false);
  }

  private void handleUserAgent(final String value) {
    if (foundContent) {
      flushCompleteGroup(true);
      foundContent = false;
    }
    currentGroup.addUserAgent(value);
  }

  private static boolean isHexChar(final byte b) {
    return Character.isDigit(b) || ('a' <= b && b <= 'f') || ('A' <= b && b <= 'F');
  }

  /**
   * Canonicalize paths: escape characters outside of US-ASCII charset (e.g. /SanJoséSellers ==>
   * /Sanjos%C3%A9Sellers) and normalize escape-characters (e.g. %aa ==> %AA)
   *
   * @param path Path to canonicalize.
   * @return escaped and normalized path
   */
  private static String maybeEscapePattern(final String path) {
    final byte[] bytes = path.getBytes(StandardCharsets.UTF_8);

    int unescapedCount = 0;
    boolean notCapitalized = false;

    // Check if any changes required
    for (int i = 0; i < bytes.length; i++) {
      if (i < bytes.length - 2
          && bytes[i] == '%'
          && isHexChar(bytes[i + 1])
          && isHexChar(bytes[i + 2])) {
        if (Character.isLowerCase(bytes[i + 1]) || Character.isLowerCase(bytes[i + 2])) {
          notCapitalized = true;
        }
        i += 2;
      } else if ((bytes[i] & 0x80) != 0) {
        unescapedCount++;
      }
    }

    // Return if no changes needed
    if (unescapedCount == 0 && !notCapitalized) {
      return path;
    }

    final StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < bytes.length; i++) {
      if (i < bytes.length - 2
          && bytes[i] == '%'
          && isHexChar(bytes[i + 1])
          && isHexChar(bytes[i + 2])) {
        stringBuilder.append((char) bytes[i++]);
        stringBuilder.append((char) Character.toUpperCase(bytes[i++]));
        stringBuilder.append((char) Character.toUpperCase(bytes[i]));
      } else if ((bytes[i] & 0x80) != 0) {
        stringBuilder.append('%');
        stringBuilder.append(Integer.toHexString((bytes[i] >> 4) & 0xf).toUpperCase());
        stringBuilder.append(Integer.toHexString(bytes[i] & 0xf).toUpperCase());
      } else {
        stringBuilder.append((char) bytes[i]);
      }
    }
    return stringBuilder.toString();
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
          foundContent = true;
          if (currentGroup.isGlobal() || currentGroup.getUserAgents().size() > 0) {
            final String path = maybeEscapePattern(directiveValue);
            currentGroup.addRule(directiveType, path);

            if (directiveType == Parser.DirectiveType.ALLOW) {
              // Google-specific optimization: 'index.htm' and 'index.html' are normalized to '/'.
              final int slashPos = path.lastIndexOf('/');

              if (slashPos != -1) {
                final String fileName = path.substring(slashPos + 1);
                if ("index.htm".equals(fileName) || "index.html".equals(fileName)) {
                  final String normalizedPath = path.substring(0, slashPos + 1) + '$';

                  if (!currentGroup.hasRule(Parser.DirectiveType.ALLOW, normalizedPath)) {
                    logger.atInfo().log(
                        "Allowing normalized path: \"%s\" -> \"%s\"",
                        directiveValue, normalizedPath);
                    currentGroup.addRule(Parser.DirectiveType.ALLOW, normalizedPath);
                  }
                }
              }
            }
          }
          break;
        }
      case SITEMAP:
      case UNKNOWN:
        {
          foundContent = true;
          break;
        }
    }
  }

  @Override
  public Matcher compute() {
    return new RobotsMatcher(robotsContents);
  }
}
