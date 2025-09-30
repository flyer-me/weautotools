# Run Spectral lint locally on Windows PowerShell
# Requires node & npm available in PATH
$target = "docs/openapi.yaml"
if (-Not (Test-Path $target)) {
  Write-Host "File $target not found. Trying docs/openapi/openapi.yaml"
  $target = "docs/openapi/openapi.yaml"
}
Write-Host "Linting $target with Spectral..."
# Install locally if not present
npm install --no-save @stoplight/spectral-cli @stoplight/spectral/rulesets/oas
npx @stoplight/spectral-cli@6 lint $target --ruleset "node_modules/@stoplight/spectral/rulesets/oas/index.json" --fail-severity error
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "Spectral finished."