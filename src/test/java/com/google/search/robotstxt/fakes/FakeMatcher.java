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

package com.google.search.robotstxt.fakes;

import com.google.search.robotstxt.Matcher;
import com.google.search.robotstxt.RobotsContents;

/**
 * Matcher that provides direct access to robots.txt contents. Used to verify parsing results.
 */
public class FakeMatcher implements Matcher {
  private RobotsContents robotsContents;

  public FakeMatcher(final RobotsContents robotsContents) {
    this.robotsContents = robotsContents;
  }

  public RobotsContents getRobotsContents() {
    return robotsContents;
  }
}
