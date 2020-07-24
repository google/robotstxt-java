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
import java.util.Objects;

/**
 * Representation of robots.txt contents: multiple groups of rules.
 */
public class RobotsContents {
  /**
   * Representation of robots.txt group of rules: multiple user-agents to which
   * multiple rules are applied.
   */
  static class Group {
    /**
     * Representation of robots.txt rule: pair of directive and value.
     */
    static class Rule {
      private final Parser.DirectiveType directiveType;
      private final String directiveValue;

      Rule(final Parser.DirectiveType directiveType, final String directiveValue) {
        this.directiveType = directiveType;
        this.directiveValue = directiveValue;
      }

      public Parser.DirectiveType getDirectiveType() {
        return directiveType;
      }

      public String getDirectiveValue() {
        return directiveValue;
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        return directiveType == rule.directiveType &&
            directiveValue.equals(rule.directiveValue);
      }

      @Override
      public int hashCode() {
        return Objects.hash(directiveType, directiveValue);
      }
    }

    private final List<String> userAgents;
    private final List<Rule> rules;

    Group() {
      userAgents = new ArrayList<>();
      rules = new ArrayList<>();
    }

    public Group(final List<String> userAgents, final List<Rule> rules) {
      this.userAgents = userAgents;
      this.rules = rules;
    }

    void addUserAgent(final String userAgent) {
      userAgents.add(userAgent);
    }

    void addRule(final Parser.DirectiveType directiveType, final String directiveValue) {
      rules.add(new Rule(directiveType, directiveValue));
    }

    public List<String> getUserAgents() {
      return userAgents;
    }

    public List<Rule> getRules() {
      return rules;
    }
  }

  private final List<Group> groups;

  RobotsContents() {
    groups = new ArrayList<>();
  }

  public RobotsContents(final List<Group> groups) {
    this.groups = groups;
  }

  void addGroup(Group group) {
    groups.add(group);
  }

  public List<Group> getGroups() {
    return groups;
  }
}
