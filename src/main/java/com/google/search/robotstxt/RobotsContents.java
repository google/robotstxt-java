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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Representation of robots.txt contents: multiple groups of rules. */
public class RobotsContents {
  /**
   * Representation of robots.txt group of rules: multiple user-agents to which multiple rules are
   * applied.
   */
  static class Group {
    /** Representation of robots.txt rule: pair of directive and value. */
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
      public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Rule other = (Rule) obj;
        return Objects.equals(directiveType, other.directiveType)
            && Objects.equals(directiveValue, other.directiveValue);
      }

      @Override
      public int hashCode() {
        return Objects.hash(directiveType, directiveValue);
      }
    }

    private final Set<String> userAgents;
    private final Set<Rule> rules;
    private boolean global = false;

    Group() {
      userAgents = new HashSet<>();
      rules = new HashSet<>();
    }

    // Intended to be used from tests only.
    Group(final List<String> userAgents, final List<Rule> rules) {
      this(userAgents, rules, false);
    }

    // Intended to be used from tests only.
    Group(final List<String> userAgents, final List<Rule> rules, final boolean global) {
      this.userAgents = new HashSet<>(userAgents);
      this.rules = new HashSet<>(rules);
      this.global = global;
    }

    void addUserAgent(final String userAgent) {
      if (userAgent.trim().equals("*")) {
        global = true;
      } else {
        int end = 0;
        for (; end < userAgent.length(); end++) {
          final char ch = userAgent.charAt(end);
          if (!Character.isAlphabetic(ch) && ch != '-' && ch != '_') {
            break;
          }
        }
        userAgents.add(userAgent.substring(0, end));
      }
    }

    void addRule(final Parser.DirectiveType directiveType, final String directiveValue) {
      rules.add(new Rule(directiveType, directiveValue));
    }

    public Set<String> getUserAgents() {
      return userAgents;
    }

    public Set<Rule> getRules() {
      return rules;
    }

    public boolean isGlobal() {
      return global;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      Group other = (Group) obj;
      return Objects.equals(userAgents, other.userAgents)
          && Objects.equals(rules, other.rules)
          && Objects.equals(global, other.global);
    }

    @Override
    public int hashCode() {
      return Objects.hash(userAgents, rules);
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
