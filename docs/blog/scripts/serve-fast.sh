#!/usr/bin/env bash
set -euo pipefail

# Serve the blog from a fast WSL-local mirror while periodically syncing edits from Windows.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SYNC_SCRIPT="${SCRIPT_DIR}/sync-to-wsl.sh"
BLOG_ROOT_DEFAULT="$(cd "${SCRIPT_DIR}/.." && pwd)/"

SRC="${1:-$BLOG_ROOT_DEFAULT}"
DEST="${2:-${HOME}/waltz-blog-dev/}"
SYNC_INTERVAL="${SYNC_INTERVAL:-1}"
JEKYLL_HOST="${JEKYLL_HOST:-127.0.0.1}"
JEKYLL_PORT="${JEKYLL_PORT:-4000}"
JEKYLL_INCREMENTAL="${JEKYLL_INCREMENTAL:-1}"

if [[ ! -x "$SYNC_SCRIPT" ]]; then
  echo "Missing or non-executable sync script: $SYNC_SCRIPT" >&2
  exit 1
fi

# Initial sync so Jekyll has a complete working tree.
"$SYNC_SCRIPT" "$SRC" "$DEST"

cd "$DEST"

# Keep gems local to the mirror.
bundle config set --local path "vendor/bundle" >/dev/null
bundle install >/dev/null

sync_loop() {
  while true; do
    "$SYNC_SCRIPT" "$SRC" "$DEST" >/dev/null
    sleep "$SYNC_INTERVAL"
  done
}

sync_loop &
SYNC_PID=$!

cleanup() {
  if kill -0 "$SYNC_PID" 2>/dev/null; then
    kill "$SYNC_PID" 2>/dev/null || true
  fi
}

trap cleanup EXIT INT TERM

echo "Serving from $DEST"
echo "Open: http://${JEKYLL_HOST}:${JEKYLL_PORT}/blog/"

JEKYLL_ARGS=(
  exec
  jekyll
  serve
  --livereload
  --host "$JEKYLL_HOST"
  --port "$JEKYLL_PORT"
)

if [[ "$JEKYLL_INCREMENTAL" == "1" ]]; then
  echo "Mode: incremental rebuilds"
  JEKYLL_ARGS+=(--incremental)
else
  echo "Mode: full rebuilds"
fi

exec bundle "${JEKYLL_ARGS[@]}"
