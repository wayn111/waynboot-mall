param(
    [string]$RepoUrl = "https://github.com/jnMetaCode/superpowers-zh.git",
    [string]$RepoDir = "$HOME\.codex\superpowers",
    [string]$SkillsLink = "$HOME\.agents\skills\superpowers"
)

$ErrorActionPreference = "Stop"

function Remove-LegacyBootstrap {
    param(
        [Parameter(Mandatory = $true)]
        [string]$AgentsFile
    )

    if (-not (Test-Path -LiteralPath $AgentsFile)) {
        return $false
    }

    $content = Get-Content -LiteralPath $AgentsFile -Raw
    if ([string]::IsNullOrWhiteSpace($content)) {
        return $false
    }

    $updated = $content
    $updated = [regex]::Replace(
        $updated,
        '(?ms)^```(?:bash|sh|powershell|pwsh)?\s*.*?superpowers-codex bootstrap.*?```\r?\n?',
        ''
    )
    $updated = [regex]::Replace(
        $updated,
        '(?m)^.*superpowers-codex bootstrap.*\r?\n?',
        ''
    )

    if ($updated -ne $content) {
        Set-Content -LiteralPath $AgentsFile -Value $updated -NoNewline
        return $true
    }

    return $false
}

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    throw "未检测到 git，请先安装 Git。"
}

$repoParent = Split-Path -Path $RepoDir -Parent
$skillsParent = Split-Path -Path $SkillsLink -Parent
$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$backupDir = Join-Path -Path $repoParent -ChildPath "superpowers-backup-$timestamp"

Write-Output "准备安装 superpowers-zh..."
Write-Output "仓库地址: $RepoUrl"
Write-Output "目标仓库目录: $RepoDir"
Write-Output "skills 连接目录: $SkillsLink"

if (Test-Path -LiteralPath $SkillsLink) {
    $linkItem = Get-Item -LiteralPath $SkillsLink
    if (-not ($linkItem.Attributes -band [IO.FileAttributes]::ReparsePoint)) {
        throw "已存在同名目录，但它不是目录连接: $SkillsLink"
    }
    Remove-Item -LiteralPath $SkillsLink -Force
    Write-Output "已移除旧的 skills 目录连接。"
}

if (Test-Path -LiteralPath $RepoDir) {
    Rename-Item -LiteralPath $RepoDir -NewName ([IO.Path]::GetFileName($backupDir))
    Write-Output "已备份旧仓库到: $backupDir"
}

New-Item -ItemType Directory -Force -Path $repoParent | Out-Null
New-Item -ItemType Directory -Force -Path $skillsParent | Out-Null

git clone $RepoUrl $RepoDir
Write-Output "已克隆 superpowers-zh 仓库。"

$skillsTarget = Join-Path -Path $RepoDir -ChildPath "skills"
$mklinkOutput = cmd /c "mklink /J `"$SkillsLink`" `"$skillsTarget`""
Write-Output $mklinkOutput

$agentsFile = "$HOME\.codex\AGENTS.md"
$removedLegacy = Remove-LegacyBootstrap -AgentsFile $agentsFile
if ($removedLegacy) {
    Write-Output "已移除 ~/.codex/AGENTS.md 中的旧 bootstrap 片段。"
} else {
    Write-Output "~/.codex/AGENTS.md 中未发现旧 bootstrap 片段。"
}

Write-Output ""
Write-Output "安装完成。"
Write-Output "如果你当前打开着 Codex，请重启会话，让新技能重新被发现。"
