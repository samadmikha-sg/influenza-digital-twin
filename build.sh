#!/usr/bin/env bash
# Usage: ./build.sh [test|run|bench]   (requires JDK 17+)
set -e
rm -rf out && mkdir -p out
javac -d out $(find src -name '*.java')
case "$1" in
  test)  javac -cp out -d out test/TestRunner.java && java -cp out TestRunner ;;
  bench) java -cp out com.samadmikha.flusim.Benchmark ;;
  run)   shift; java -cp out com.samadmikha.flusim.Main "$@" ;;
  *)     echo "Built into ./out. Try: ./build.sh test | run 100000 120 42 results.csv | bench" ;;
esac
