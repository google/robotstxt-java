# Google Robots.txt Parser and Matcher Library in Java

This project aims to implement the robots.txt parser and matcher in Java. It is
based on the [C++ implementation](https://github.com/google/robotstxt).

## About the library

The Robots Exclusion Protocol (REP) is a standard that enables website owners
to control which URLs may be accessed by automated clients (i.e. crawlers)
through a simple text file with a specific syntax. It's one of the basic
building blocks of the internet as we know it and what allows search engines
to operate.

Because the REP was only a de-facto standard for the past 25 years, different
implementers implement parsing of robots.txt slightly differently, leading to
confusion. This project aims to fix that by releasing the parser that Google
uses.

The library is a Java port of 
[C++ parser and matcher](https://github.com/google/robotstxt) which is a
slightly modified production code used by Googlebot, Google's crawler. The
library is released open-source to help developers build tools that better
reflect Google's robots.txt parsing and matching.

For webmasters, we included a runnable class `RobotsParserApp` which is a small
application that allows testing a single URL and several user-agents against a
robots.txt.

## Development

### Prerequisites

You need Maven to build this project.
[Download](https://maven.apache.org/download.html) and
[install](https://maven.apache.org/install.html) it from the official website.

You can also install it like this if your Linux supports it:

```
$ sudo apt-get install maven
```

### Build it

#### Using Maven

Standard maven commands work here.

```
$ mvn install
```

Or if you want a build from scratch:

```
$ mvn clean install
```

#### Using Maven Assembly Plugin

Alternatively, you can compile the entire project into a single JAR using the
following command:

```
$ mvn clean compile assembly:single
```

You can find the result in `target` directory. 

### Run it

#### Using Maven

Following commands will run an application that parses given robots.txt file
and print a matching verdict: `ALLOWED` or `DISALLOWED` (exit codes are `0`
and `1` respectively). 

You should provide a target URL using `-u` (`--url`) flag. At least one agent
must be specified using `-a` (`--agent`) flag (verdict `DISALLOWED` is printed
iff none of the user-agents are allowed to crawl given URL).

When flag `-f` (`--file`) is omitted, robots.txt contents are expected to be
received via standard input:

```
$ mvn exec:java -Dexec.mainClass=com.google.search.robotstxt.RobotsParserApp -Dexec.args="--agent FooBot --url http://foo.com/bar"
```

If you want the application to read an existing robots.txt file, use flag `-f`
(`--file`):

```
$ mvn exec:java -Dexec.mainClass=com.google.search.robotstxt.RobotsParserApp -Dexec.args="--agent FooBot --url http://foo.com/bar --file path/to/robots.txt"
```

#### From JAR

If you have built the project into JAR, you can run it from there (reading
robots.txt from standard input):

```
$ java -jar target/robotstxt-java-1.0-SNAPSHOT-jar-with-dependencies.jar --agent FooBot --url http://foo.com/bar
```

Or (reading from file):

```
$ java -jar target/robotstxt-java-1.0-SNAPSHOT-jar-with-dependencies.jar --agent FooBot --url http://foo.com/bar --file path/to/robots.txt
```

## Notes

Parsing of robots.txt files themselves is done exactly as in the production
version of Googlebot, including how percent codes and unicode characters in
patterns are handled. The user must ensure however that the URI passed to the
`Matcher` methods, or to the `--url` parameter of the application, follows the
format specified by RFC3986, since this library will not perform full
normalization of those URI parameters. Only if the URI is in this format, the
matching will be done according to the REP specification.

## License

The robots.txt parser and matcher Java library is licensed under the terms of
the Apache license. See LICENSE for more information.

## Source Code Headers

Every file containing source code must include copyright and license
information. This includes any JS/CSS files that you might be serving out to
browsers. (This is to help well-intentioned people avoid accidental copying
that doesn't comply with the license.)

Apache header:

    Copyright 2020 Google LLC

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

It can be done easily by using the
[addlicense](https://github.com/google/addlicense) tool.

Install it:

```
$ go get -u github.com/google/addlicense
```

Use it like this to make sure all files have the licence:

```
$ ~/go/bin/addlicense -c "Google LLC" -l apache .
```
