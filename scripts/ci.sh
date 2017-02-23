#!/bin/bash
set -e
set -o pipefail

sbt +lint:compile
sbt +test
