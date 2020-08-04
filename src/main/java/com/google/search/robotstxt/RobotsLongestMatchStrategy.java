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

/**
 * Implementation of matching strategy used in robots.txt matching. Implements longest-match
 * strategy.
 */
public class RobotsLongestMatchStrategy implements MatchingStrategy {
  /**
   * Checks whether the given path may be matched to the given pattern. Treats '*' as a wildcard and
   * '$' as a termination symbol iff it is in the end of pattern.
   *
   * @param path path to match
   * @param pattern pattern to match to
   * @return {@code true} iff given path matches given pattern
   */
  private static boolean matches(final String path, final String pattern) {
    // "Prefixes" array stores "path" prefixes that match specific prefix of "pattern".
    // Prefixes of "pattern" are iterated over in ascending order in the loop below.
    // Each prefix is represented by its end index (exclusive), the array stores them in ascending
    // order.
    final int[] prefixes = new int[path.length() + 1];
    prefixes[0] = 0;
    int prefixesCount = 1;

    for (int i = 0; i < pattern.length(); i++) {
      final char ch = pattern.charAt(i);

      // '$' in the end of pattern indicates its termination.
      if (ch == '$' && i + 1 == pattern.length()) {
        return prefixes[prefixesCount - 1] == path.length();
      }

      // In case of '*' occurrence all path prefixes starting from the shortest one may be matched.
      if (ch == '*') {
        prefixesCount = path.length() - prefixes[0] + 1;
        for (int j = 1; j < prefixesCount; j++) {
          prefixes[j] = prefixes[j - 1] + 1;
        }
      } else {
        // Iterate over each previous prefix and try to extend by one character.
        int newPrefixesCount = 0;
        for (int j = 0; j < prefixesCount; j++) {
          if (prefixes[j] < path.length() && path.charAt(prefixes[j]) == ch) {
            prefixes[newPrefixesCount++] = prefixes[j] + 1;
          }
        }
        if (newPrefixesCount == 0) {
          return false;
        }
        prefixesCount = newPrefixesCount;
      }
    }

    return true;
  }

  @Override
  public int matchAllowPriority(String path, String pattern) {
    return matches(path, pattern) ? pattern.length() : -1;
  }

  @Override
  public int matchDisallowPriority(String path, String pattern) {
    return matches(path, pattern) ? pattern.length() : -1;
  }
}
