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
