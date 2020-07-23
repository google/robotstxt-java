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
  @Override
  public int matchAllowPriority(String targetUrl, String directiveValue) {
    return 0;
  }

  @Override
  public int matchDisallowPriority(String targetUrl, String directiveValue) {
    return 0;
  }
}