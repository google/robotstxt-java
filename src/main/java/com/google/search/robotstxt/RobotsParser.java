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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Stream;

/** Robots.txt parser implementation. */
public class RobotsParser extends Parser {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private final int valueMaxLengthBytes;

  public RobotsParser(final ParseHandler parseHandler) {
    super(parseHandler);
    this.valueMaxLengthBytes = 2083;
  }

  RobotsParser(final ParseHandler parseHandler, final int valueMaxLengthBytes) {
    super(parseHandler);
    this.valueMaxLengthBytes = valueMaxLengthBytes;
  }

  private static boolean isWhitespace(final char ch) {
    return ch == ' ' || ch == '\t';
  }

  /**
   * Extracts substring between given indexes and trims preceding and succeeding whitespace
   * characters.
   *
   * @param bytes data to extract from
   * @param beginIndex the beginning index, inclusive
   * @param endIndex the ending index, exclusive
   * @return extracted substring with trimmed whitespaces
   * @throws ParseException if there are only whitespace characters between given indexes
   */
  private static String trimBounded(final byte[] bytes, final int beginIndex, final int endIndex)
      throws ParseException {
    int begin = beginIndex;
    int end = endIndex;
    while (begin < endIndex && isWhitespace((char) bytes[begin])) {
      begin++;
    }
    while (end > beginIndex && isWhitespace((char) bytes[end - 1])) {
      end--;
    }
    if (begin >= end) {
      throw new ParseException();
    } else {
      return new String(Arrays.copyOfRange(bytes, begin, end), StandardCharsets.UTF_8);
    }
  }

  private static DirectiveType parseDirective(final String key) {
    if (key.equalsIgnoreCase("user-agent")) {
      return DirectiveType.USER_AGENT;
    } else {
      try {
        return DirectiveType.valueOf(key.toUpperCase());
      } catch (final IllegalArgumentException e) {
        final boolean disallowTypoDetected =
            Stream.of("dissallow", "dissalow", "disalow", "diasllow", "disallaw")
                .anyMatch(s -> key.compareToIgnoreCase(s) == 0);
        if (disallowTypoDetected) {
          logger.atInfo().log("Fixed typo: \"%s\" -> \"%s\"", key, "disallow");
          return DirectiveType.DISALLOW;
        }

        return DirectiveType.UNKNOWN;
      }
    }
  }

  private static void log(
      final Level level,
      final String message,
      final byte[] robotsTxtBodyBytes,
      final int lineBegin,
      final int lineEnd,
      final int lineNumber) {
    logger.at(level).log(
        "%s%nAt line %d:%n%s\t",
        message,
        lineNumber,
        new String(Arrays.copyOfRange(robotsTxtBodyBytes, lineBegin, lineEnd)));
  }

  /**
   * Extracts value from robots.txt body and trims it to {@link this#valueMaxLengthBytes} bytes if
   * necessary. Most of parameters are used for logging.
   *
   * @param robotsTxtBodyBytes contents of robots.txt file
   * @param separator index of separator between key and value
   * @param limit index of key and value ending
   * @param lineBegin index of line beginning
   * @param lineEnd index of line ending
   * @param lineNumber number of line in robots.txt file
   * @return parsed value within given line of robots.txt
   * @throws ParseException if line limits are invalid
   */
  private String getValue(
      final byte[] robotsTxtBodyBytes,
      final int separator,
      final int limit,
      final int lineBegin,
      final int lineEnd,
      final int lineNumber)
      throws ParseException {
    String value = trimBounded(robotsTxtBodyBytes, separator + 1, limit);

    // Google-specific optimization: since no search engine will process more than 2083 bytes
    // per URL all values are trimmed to fit this size.
    final byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);

    // We decrease max size by two bytes. It is done to fit a replacement character (\uFFFD)
    // if the last character is trimmed to an invalid one.
    final int maxLengthBytes = valueMaxLengthBytes - 2;

    if (valueBytes.length > maxLengthBytes) {
      log(
          Level.INFO,
          "Value truncated to " + valueMaxLengthBytes + " bytes.",
          robotsTxtBodyBytes,
          lineBegin,
          lineEnd,
          lineNumber);

      value =
          new String(
              valueBytes, 0, Math.min(valueBytes.length, maxLengthBytes), StandardCharsets.UTF_8);
    }

    return value;
  }

  private void parseLine(
      final byte[] robotsTxtBodyBytes,
      final int lineBegin,
      final int lineEnd,
      final int lineNumber) {
    int limit = lineEnd;
    int separator = lineEnd;
    int whitespaceSeparator = lineEnd;
    boolean hasContents = false;

    for (int i = lineBegin; i < lineEnd; i++) {
      final byte b = robotsTxtBodyBytes[i];
      if (b == '#') {
        limit = i;
        break;
      }
      if (!isWhitespace((char) b)) {
        hasContents = true;
      }
      if (isWhitespace((char) b) && hasContents && whitespaceSeparator == lineEnd) {
        whitespaceSeparator = i;
      }
      if (separator == lineEnd && b == ':') {
        separator = i;
      }
    }

    if (separator == lineEnd) {
      // Google-specific optimization: some people forget the colon, so we need to
      // accept whitespace instead.
      if (whitespaceSeparator != lineEnd) {
        log(
            Level.INFO,
            "Assuming whitespace as a separator.",
            robotsTxtBodyBytes,
            lineBegin,
            lineEnd,
            lineNumber);
        separator = whitespaceSeparator;
      } else {
        if (hasContents) {
          log(
              Level.WARNING,
              "No separator found.",
              robotsTxtBodyBytes,
              lineBegin,
              lineEnd,
              lineNumber);
        }
        return;
      }
    }

    final String key;
    try {
      key = trimBounded(robotsTxtBodyBytes, lineBegin, separator);
    } catch (ParseException e) {
      log(Level.WARNING, "No key found.", robotsTxtBodyBytes, lineBegin, lineEnd, lineNumber);
      return;
    }

    DirectiveType directiveType = parseDirective(key);
    if (directiveType == DirectiveType.UNKNOWN) {
      log(Level.WARNING, "Unknown key.", robotsTxtBodyBytes, lineBegin, lineEnd, lineNumber);
    }

    String value;
    try {
      value = getValue(robotsTxtBodyBytes, separator, limit, lineBegin, lineEnd, lineNumber);
    } catch (final ParseException e) {
      log(Level.WARNING, "No value found.", robotsTxtBodyBytes, lineBegin, lineEnd, lineNumber);
      value = "";
      directiveType = DirectiveType.UNKNOWN;
    }
    parseHandler.handleDirective(directiveType, value);
  }

  @Override
  public Matcher parse(byte[] robotsTxtBodyBytes) {
    final byte[] bomUtf8 = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    int bomPos = 0;

    int posBegin = 0;
    int posEnd = 0;
    int lineNumber = 0;
    boolean previousWasCarriageReturn = false;

    parseHandler.handleStart();

    // Iteration over characters is preferred over utilities that split text into lines to avoid
    // having to create additional Strings and comply with line breaking defined in standard.
    for (int i = 0; i <= robotsTxtBodyBytes.length; i++) {
      final byte b = (i == robotsTxtBodyBytes.length) ? (byte) '\0' : robotsTxtBodyBytes[i];

      // Google-specific optimization: UTF-8 byte order marks should never
      // appear in a robots.txt file, but they do nevertheless. Skipping
      // possible BOM-prefix in the first bytes of the input.
      if (bomPos < bomUtf8.length && b == bomUtf8[bomPos++]) {
        posBegin++;
        posEnd++;
        continue;
      }
      bomPos = bomUtf8.length;

      if (b != '\n' && b != '\r' && b != '\0') {
        posEnd++;
      } else {
        if (posBegin != posEnd || !previousWasCarriageReturn || b != '\n') {
          parseLine(robotsTxtBodyBytes, posBegin, posEnd, ++lineNumber);
        }
        posBegin = posEnd = i + 1;
        previousWasCarriageReturn = b == '\r';
      }
    }

    parseHandler.handleEnd();

    return parseHandler.compute();
  }
}
