#!/usr/bin/env bash

(
  mkdir jars || true
  cd jars || exit 0
  curl -JO -L https://versions.velocitypowered.com/download/1.1.0.jar
)
