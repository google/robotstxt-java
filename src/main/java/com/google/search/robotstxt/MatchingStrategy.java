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
 * Provides methods to calculate match priority for given directives against given path. It is
 * required to compute match verdict in {@link RobotsMatcher}.
 */
public interface MatchingStrategy {
  /**
   * Calculates priority of ALLOW verdict based on given directive.
   *
   * @param path path to calculate ALLOW match priority against
   * @param pattern ALLOW directive value
   * @return match priority (higher value means higher chance of ALLOW verdict)
   */
  int matchAllowPriority(final String path, final String pattern);

  /**
   * Calculates priority of DISALLOW verdict based on given directive.
   *
   * @param path path to calculate DISALLOW match priority against
   * @param pattern DISALLOW directive value
   * @return match priority (higher value means higher chance of DISALLOW verdict)
   */
  int matchDisallowPriority(final String path, final String pattern);
}
