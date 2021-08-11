#!/usr/bin/env sh

echo "JAR_FILE: ${JAR_FILE}"
echo "Args:     $@"

java -jar /financemanager/${JAR_FILE} $@