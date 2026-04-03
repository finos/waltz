#!/usr/bin/env bash
set -euo pipefail

# Incrementally sync blog sources from Windows mount to a fast WSL-local mirror.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BLOG_ROOT_DEFAULT="$(cd "${SCRIPT_DIR}/.." && pwd)/"
SRC_DEFAULT="$BLOG_ROOT_DEFAULT"
DEST_DEFAULT="${HOME}/waltz-blog-dev/"

SRC="${1:-$SRC_DEFAULT}"
DEST="${2:-$DEST_DEFAULT}"
DRY_RUN="${DRY_RUN:-0}"

if [[ ! -d "$SRC" ]]; then
  echo "Source path does not exist: $SRC" >&2
  exit 1
fi

mkdir -p "$DEST"

RSYNC_ARGS=(
  -a
  --delete
  --info=stats2
  --exclude=.git/
  --exclude=.bundle/
  --exclude=.jekyll-cache/
  --exclude=.jekyll-metadata
  --exclude=_site/
  --exclude=vendor/bundle/
)

if [[ "$DRY_RUN" == "1" ]]; then
  RSYNC_ARGS+=(--dry-run)
fi

echo "Syncing blog source"
echo "  from: $SRC"
echo "    to: $DEST"

rsync "${RSYNC_ARGS[@]}" "$SRC" "$DEST"

echo "Sync complete"
