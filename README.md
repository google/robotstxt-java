# Google Robots.txt Parser and Matcher Library in Java

This project aims to implement the robots.txt parser and matcher in Java. It is
based on the [C++ implementation](https://github.com/google/robotstxt).

## Development

### Prerequisites

You need Maven to build this project.
[Download](https://maven.apache.org/download.html) and
[install](https://maven.apache.org/install.html) it from the official website.

You can also install it like this if your Linux supports it:

```
$ sudo apt-get install maven
```

You will also need to have the
[protocol buffer](https://developers.google.com/protocol-buffers/) compiler
([protoc](https://github.com/protocolbuffers/protobuf)) installed.
Get [the latest](https://github.com/protocolbuffers/protobuf/releases/latest)
for your platform (i.e. `protoc-x.x.x-win64.zip`, `protoc-x.x.x-osx-x86_64.zip`).

You can also install it like this if your Linux supports it:

```
$ sudo apt-get install protobuf-compiler
```

### Build it

Standard maven commands work here.

```
$ mvn install
```

Or if you want a build from scratch:

```
$ mvn clean install
```

### Run it

```
$ mvn exec:java -Dexec.mainClass="com.google.search.robotstxt.App" -Dexec.args="arg0 arg1"
```

## Source Code Headers

Every file containing source code must include copyright and license
information. This includes any JS/CSS files that you might be serving out to
browsers. (This is to help well-intentioned people avoid accidental copying that
doesn't comply with the license.)

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
[addlicense](https://github.com/google/addlicense) tool

Install it:

```
$ go get -u github.com/google/addlicense
```

Use it like this to make sure all files have the licence:

```
$ ~/go/bin/addlicense -c "Google LLC" -l apache .
```
