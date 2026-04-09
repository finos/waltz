# Fast Local Blog Workflow (Windows edit + WSL build)

Use these scripts when editing in the Windows workspace and serving Jekyll from a faster WSL-local mirror.

## Scripts

- `scripts/sync-to-wsl.sh`: one-shot incremental sync from Windows mount to WSL
- `scripts/serve-fast.sh`: periodic sync + `jekyll serve --incremental`

## One-time setup (WSL)

```bash
chmod +x ./scripts/*.sh
```

## Dry-run sync check

```bash
DRY_RUN=1 ./scripts/sync-to-wsl.sh
```

## Start fast local server

```bash
./scripts/serve-fast.sh
```

The blog is served from the mirrored path at:

- `http://127.0.0.1:4000/blog/`

## Optional custom paths

```bash
./scripts/serve-fast.sh \
  "$(pwd)/" \
  ~/waltz-blog-dev/
```

## Optional sync frequency

```bash
SYNC_INTERVAL=2 ./scripts/serve-fast.sh
```

## Optional host and port

```bash
JEKYLL_HOST=0.0.0.0 JEKYLL_PORT=4001 ./scripts/serve-fast.sh
```

