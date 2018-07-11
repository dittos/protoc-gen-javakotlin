#!/bin/sh
base=$(dirname $0)
exec java -jar $base/build/libs/protoc-gen-javakotlin*.jar
