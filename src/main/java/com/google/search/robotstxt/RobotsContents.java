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

import java.util.ArrayList;
import java.util.List;

public class RobotsContents {
  static class Group {
    static class Rule {
      private final Parser.DirectiveType directiveType;
      private final String directiveValue;

      Rule(final Parser.DirectiveType directiveType, final String directiveValue) {
        this.directiveType = directiveType;
        this.directiveValue = directiveValue;
      }
    }

    private final List<String> userAgents;
    private final List<Rule> rules;

    Group() {
      userAgents = new ArrayList<>();
      rules = new ArrayList<>();
    }

    void addUserAgent(final String userAgent) {
      userAgents.add(userAgent);
    }

    void addRule(final Parser.DirectiveType directiveType, final String directiveValue) {
      rules.add(new Rule(directiveType, directiveValue));
    }

    int countOfRules() {
      return rules.size();
    }
  }

  private final List<Group> groups;

  RobotsContents() {
    groups = new ArrayList<>();
  }

  void addGroup(Group group) {
    groups.add(group);
  }
}
