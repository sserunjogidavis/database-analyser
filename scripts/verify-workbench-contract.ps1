[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Get-NormalizedTextSha256 {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    $bytes = [System.IO.File]::ReadAllBytes($Path)
    $text = [System.Text.Encoding]::UTF8.GetString($bytes)
    $normalizedText = $text.Replace("`r`n", "`n").Replace("`r", "`n")
    $normalizedBytes = [System.Text.UTF8Encoding]::new($false).GetBytes($normalizedText)
    $sha256 = [System.Security.Cryptography.SHA256]::Create()
    try {
        $hash = [System.BitConverter]::ToString($sha256.ComputeHash($normalizedBytes))
        return $hash.Replace("-", "").ToLowerInvariant()
    }
    finally {
        $sha256.Dispose()
    }
}

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$contractRoot = Join-Path $repositoryRoot "contracts/workbench-catalog"
$lockPath = Join-Path $contractRoot "contract-lock.json"

if (-not (Test-Path -LiteralPath $lockPath -PathType Leaf)) {
    throw "Contract lock is missing: $lockPath"
}

$lock = Get-Content -LiteralPath $lockPath -Raw | ConvertFrom-Json
if ($lock.lockFormatVersion -ne 1) {
    throw "Unsupported contract lock format: $($lock.lockFormatVersion)"
}

if ($lock.source.commit -notmatch '^[0-9a-f]{40}$') {
    throw "The pinned Workbench commit must be a full lowercase Git commit hash."
}

if ($lock.bundle.hashAlgorithm -ne "SHA-256") {
    throw "Unsupported contract hash algorithm: $($lock.bundle.hashAlgorithm)"
}

$manifestPath = Join-Path $contractRoot $lock.bundle.manifest
$snapshotRoot = Join-Path $contractRoot $lock.bundle.snapshotDirectory

if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) {
    throw "Contract manifest is missing: $manifestPath"
}

if (-not (Test-Path -LiteralPath $snapshotRoot -PathType Container)) {
    throw "Contract snapshot is missing: $snapshotRoot"
}

$manifestHash = Get-NormalizedTextSha256 -Path $manifestPath
if ($manifestHash -cne $lock.bundle.manifestSha256) {
    throw "Contract manifest hash mismatch. Expected $($lock.bundle.manifestSha256), found $manifestHash."
}

$snapshotFullPath = [System.IO.Path]::GetFullPath($snapshotRoot)
$snapshotPrefix = $snapshotFullPath.TrimEnd(
    [System.IO.Path]::DirectorySeparatorChar,
    [System.IO.Path]::AltDirectorySeparatorChar
) + [System.IO.Path]::DirectorySeparatorChar
$expectedFiles = [System.Collections.Generic.Dictionary[string, string]]::new(
    [System.StringComparer]::Ordinal
)

$lineNumber = 0
foreach ($line in Get-Content -LiteralPath $manifestPath) {
    $lineNumber++
    if ([string]::IsNullOrWhiteSpace($line)) {
        continue
    }

    if ($line -notmatch '^([0-9a-f]{64})  (.+)$') {
        throw "Invalid manifest entry at line $lineNumber."
    }

    $expectedHash = $Matches[1]
    $relativePath = $Matches[2]

    if ([System.IO.Path]::IsPathRooted($relativePath) -or
        $relativePath.Contains('\') -or
        $relativePath.Split('/') -contains '..') {
        throw "Unsafe contract path in manifest: $relativePath"
    }

    if ($expectedFiles.ContainsKey($relativePath)) {
        throw "Duplicate contract path in manifest: $relativePath"
    }

    $candidatePath = [System.IO.Path]::GetFullPath(
        (Join-Path $snapshotRoot ($relativePath.Replace('/', [System.IO.Path]::DirectorySeparatorChar)))
    )
    if (-not $candidatePath.StartsWith($snapshotPrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Contract path escapes the snapshot: $relativePath"
    }

    if (-not (Test-Path -LiteralPath $candidatePath -PathType Leaf)) {
        throw "Pinned contract file is missing: $relativePath"
    }

    $actualHash = Get-NormalizedTextSha256 -Path $candidatePath
    if ($actualHash -cne $expectedHash) {
        throw "Pinned contract file hash mismatch: $relativePath"
    }

    $expectedFiles.Add($relativePath, $expectedHash)
}

if ($expectedFiles.Count -ne [int]$lock.bundle.fileCount) {
    throw "Contract file count mismatch. Lock declares $($lock.bundle.fileCount), manifest lists $($expectedFiles.Count)."
}

$actualFiles = @(Get-ChildItem -LiteralPath $snapshotRoot -Recurse -File)
if ($actualFiles.Count -ne $expectedFiles.Count) {
    throw "Snapshot contains $($actualFiles.Count) files, but the manifest lists $($expectedFiles.Count)."
}

foreach ($file in $actualFiles) {
    $relativePath = $file.FullName.Substring($snapshotPrefix.Length).Replace('\', '/')
    if (-not $expectedFiles.ContainsKey($relativePath)) {
        throw "Snapshot contains an unlisted contract file: $relativePath"
    }
}

Write-Host "Verified Workbench Application Catalog contract $($lock.source.contractVersion)"
Write-Host "Source commit: $($lock.source.commit)"
Write-Host "Verified files: $($expectedFiles.Count)"
