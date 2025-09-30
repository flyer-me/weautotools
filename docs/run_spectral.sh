#!/usr/bin/env bash
# Run Spectral locally (Unix-like)
TARGET="docs/openapi.yaml"
if [ ! -f "$TARGET" ]; then
  echo "File $TARGET not found, trying docs/openapi/openapi.yaml"
  TARGET="docs/openapi/openapi.yaml"
fi
echo "Linting $TARGET with Spectral..."
npm install --no-save @stoplight/spectral-cli @stoplight/spectral/rulesets/oas
npx @stoplight/spectral-cli@6 lint "$TARGET" --ruleset "node_modules/@stoplight/spectral/rulesets/oas/index.json" --fail-severity error
exit $?
