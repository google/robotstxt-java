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

public class RobotsParser extends Parser {
  public RobotsParser(ParseHandler parseHandler) {
    super(parseHandler);
  }

  private static boolean isWhitespace(final char c) {
    return c == 0x09 || c == 0x20;
  }

  private static String trimBounded(final String string, final int beginBound,
                                    final int endBound) throws ParseException {
    int begin = beginBound;
    int end = endBound;
    while (begin < endBound && isWhitespace(string.charAt(begin))) {
      begin++;
    }
    while (end > beginBound && isWhitespace(string.charAt(end - 1))) {
      end--;
    }
    if (begin >= end) {
      throw new ParseException();
    } else {
      return string.substring(begin, end);
    }
  }

  private static DirectiveType parseDirective(final String key) {
    if (key.equalsIgnoreCase("user-agent")) {
      return DirectiveType.USER_AGENT;
    } else {
      try {
        return DirectiveType.valueOf(key.toUpperCase());
      } catch (IllegalArgumentException e) {
        return DirectiveType.UNKNOWN;
      }
    }
  }

  private void parseLine(final String robotsTxtBody, final int lineBegin,
                                final int lineEnd) {
    int limit = lineEnd;
    int separator = lineEnd;

    for (int i = lineBegin; i < lineEnd; i++) {
      if (separator == lineEnd && robotsTxtBody.charAt(i) == ':') {
        separator = i;
      }
      if (robotsTxtBody.charAt(i) == '#') {
        limit = i;
        break;
      }
    }

    if (separator == lineEnd) {
      return;
    }

    try {
      final String key = trimBounded(robotsTxtBody, lineBegin, separator);
      final DirectiveType directiveType = parseDirective(key);
      final String value = trimBounded(robotsTxtBody, separator + 1, limit);
      parseHandler.handleDirective(directiveType, value);
    } catch (ParseException e) {
      // Ignored.
    }
  }

  @Override
  RobotsMatcher parse(String robotsTxtBody) {
    int posBegin = 0;
    int posEnd = 0;
    boolean previousWasCarriageReturn = false;

    parseHandler.handleStart();

    for (int i = 0; i < robotsTxtBody.length(); i++) {
      final char ch = robotsTxtBody.charAt(i);

      if (ch != 0x0A && ch != 0x0D) {
        posEnd++;
      } else {
        if (posBegin != posEnd || !previousWasCarriageReturn || ch != 0x0A) {
          parseLine(robotsTxtBody, posBegin, posEnd);
        }
        posBegin = posEnd = i + 1;
        previousWasCarriageReturn = ch == 0x0D;
      }
    }

    parseHandler.handleEnd();

    return parseHandler.compute();
  }
}
