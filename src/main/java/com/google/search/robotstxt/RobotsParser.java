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

/** Robots.txt parser implementation. */
public class RobotsParser extends Parser {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  public RobotsParser(ParseHandler parseHandler) {
    super(parseHandler);
  }

  private static boolean isWhitespace(final char ch) {
    return ch == 0x09 || ch == 0x20;
  }

  private static String trimBounded(final String string, final int beginBound, final int endBound)
      throws ParseException {
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

  private static void logWarning(
      final String message,
      final String robotsTxtBody,
      final int lineBegin,
      final int lineEnd,
      final int lineNumber) {
    logger.atWarning().log(
        message + "%nAt line %d:%n\t" + robotsTxtBody.substring(lineBegin, lineEnd), lineNumber);
  }

  private void parseLine(
      final String robotsTxtBody, final int lineBegin, final int lineEnd, final int lineNumber) {
    int limit = lineEnd;
    int separator = lineEnd;
    boolean hasContents = false;

    for (int i = lineBegin; i < lineEnd; i++) {
      final char ch = robotsTxtBody.charAt(i);
      if (!isWhitespace(ch)) {
        hasContents = true;
      }
      if (separator == lineEnd && ch == ':') {
        separator = i;
      }
      if (ch == '#') {
        limit = i;
        break;
      }
    }

    if (separator == lineEnd) {
      if (hasContents) {
        logWarning("No separator found.", robotsTxtBody, lineBegin, lineEnd, lineNumber);
      }
      return;
    }

    final String key;
    try {
      key = trimBounded(robotsTxtBody, lineBegin, separator);
    } catch (ParseException e) {
      logWarning("No key found.", robotsTxtBody, lineBegin, lineEnd, lineNumber);
      return;
    }
    final String value;
    try {
      value = trimBounded(robotsTxtBody, separator + 1, limit);
    } catch (ParseException e) {
      logWarning("No value found.", robotsTxtBody, lineBegin, lineEnd, lineNumber);
      return;
    }
    final DirectiveType directiveType = parseDirective(key);
    if (directiveType == DirectiveType.UNKNOWN) {
      logWarning("Unknown key.", robotsTxtBody, lineBegin, lineEnd, lineNumber);
    }
    parseHandler.handleDirective(directiveType, value);
  }

  @Override
  Matcher parse(String robotsTxtBody) {
    int posBegin = 0;
    int posEnd = 0;
    int lineNumber = 0;
    boolean previousWasCarriageReturn = false;

    parseHandler.handleStart();

    for (int i = 0; i < robotsTxtBody.length(); i++) {
      final char ch = robotsTxtBody.charAt(i);

      if (ch != 0x0A && ch != 0x0D) {
        posEnd++;
      } else {
        if (posBegin != posEnd || !previousWasCarriageReturn || ch != 0x0A) {
          parseLine(robotsTxtBody, posBegin, posEnd, ++lineNumber);
        }
        posBegin = posEnd = i + 1;
        previousWasCarriageReturn = ch == 0x0D;
      }
    }

    parseHandler.handleEnd();

    return parseHandler.compute();
  }
}
