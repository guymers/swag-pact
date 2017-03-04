#!/bin/bash
set -e
set -o pipefail

sbt scalafmtTest
sbt +lint:compile
sbt +test
