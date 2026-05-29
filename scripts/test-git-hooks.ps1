$ErrorActionPreference = 'Stop'

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$hookPath = Join-Path $repoRoot '.githooks/pre-commit'
if (-not (Test-Path -LiteralPath $hookPath)) {
    throw "Missing hook: .githooks/pre-commit"
}
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("waynboot-hook-test-" + [Guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Path $tempRoot | Out-Null

try {
    git -C $tempRoot init | Out-Null
    git -C $tempRoot config user.email "hook-test@example.invalid"
    git -C $tempRoot config user.name "Hook Test"
    git -C $tempRoot config core.autocrlf false

    $targetHookDir = Join-Path $tempRoot '.githooks'
    New-Item -ItemType Directory -Path $targetHookDir | Out-Null
    Copy-Item -LiteralPath $hookPath -Destination (Join-Path $targetHookDir 'pre-commit')
    git -C $tempRoot config core.hooksPath .githooks

    $resourcesDir = Join-Path $tempRoot 'demo/src/main/resources'
    New-Item -ItemType Directory -Path $resourcesDir -Force | Out-Null
    [System.IO.File]::WriteAllText((Join-Path $resourcesDir 'application-dev.yml'), "server: local`n", $utf8NoBom)
    git -C $tempRoot add .

    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    $blockedOutput = & git -C $tempRoot commit -m "add dev config" 2>&1
    $blockedExitCode = $LASTEXITCODE
    $ErrorActionPreference = $previousErrorActionPreference
    if ($blockedExitCode -eq 0) {
        throw "Expected application-dev commit to be blocked, but it succeeded."
    }
    if (($blockedOutput -join "`n") -notmatch 'application-dev') {
        throw "Hook blocked the commit without explaining application-dev."
    }

    git -C $tempRoot reset --hard | Out-Null
    [System.IO.File]::WriteAllText((Join-Path $tempRoot 'README.md'), "# hook test`n", $utf8NoBom)
    git -C $tempRoot add README.md
    git -C $tempRoot commit -m "add readme" | Out-Null
}
finally {
    if (Test-Path -LiteralPath $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force
    }
}
