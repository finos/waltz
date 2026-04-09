# Waltz Blog

This folder contains the Jekyll-based Waltz blog served under `/blog`.

## What Lives Here

- `_posts/`: blog posts
- `_layouts/`: page and post layouts
- `_sass/`: custom blog styling layered on top of the theme
- `scripts/`: local helper scripts for faster blog development in WSL

Shared Jekyll config and reusable includes now live one level up under `docs/`.

Internal blog links are environment-aware:

- local builds fall back to root-relative local paths
- GitHub Pages fork builds use `site.github.url`
- FINOS production continues to work at the custom domain root

## Running Locally

From `docs/`:

```bash
BUNDLE_GEMFILE=blog/Gemfile bundle install
BUNDLE_GEMFILE=blog/Gemfile bundle exec jekyll serve --config _config.yml --livereload --incremental
```

Why this runs from `docs/`:

- `docs/_config.yml` is the single Jekyll config used by both local development and GitHub Pages
- shared blog includes live in `docs/_includes/`
- the blog content itself still lives in `docs/blog/`
- `BUNDLE_GEMFILE=blog/Gemfile` keeps Bundler pointed at the blog Gemfile while serving from the docs root
- `docs/_config.yml` excludes `blog/vendor/` and `blog/.bundle/` so Jekyll does not try to treat Bundler internals as site content
- `docs/_config.yml` and the sync script exclude `.sass-cache/` so Sass cache files do not trigger endless rebuild loops

Default local URL:

- `http://127.0.0.1:4000/blog/`

## Fast WSL Workflow

The `scripts/` directory contains helper scripts for editing in the repo while serving from a faster WSL-local mirror.

- `scripts/sync-to-wsl.sh`: one-shot sync of `docs/` into a local mirror
- `scripts/serve-fast.sh`: repeated sync plus `jekyll serve --config _config.yml`

Typical usage:

```bash
chmod +x ./scripts/*.sh
./scripts/serve-fast.sh
```

By default `serve-fast.sh` uses incremental rebuilds for quicker feedback.

Use a full rebuild when changing:

- post `categories` or `permalink`
- `_config.yml`
- `_layouts/`
- `_includes/`
- other changes that affect generated URLs or shared page output

Run a full rebuild with:

```bash
JEKYLL_INCREMENTAL=0 ./scripts/serve-fast.sh
```

Optional host and port:

```bash
JEKYLL_HOST=0.0.0.0 JEKYLL_PORT=4001 ./scripts/serve-fast.sh
```

How `serve-fast.sh` works now:

- syncs the `docs/` directory into a local mirror
- starts Jekyll from the mirrored `docs/` root
- uses `BUNDLE_GEMFILE=blog/Gemfile` so Ruby dependencies still come from the blog Gemfile
- serves with the shared root config: `docs/_config.yml`

## Writing Posts

- Add new posts in `_posts/`
- Use Jekyll post naming: `YYYY-MM-DD-title.md`
- Use `<!--more-->` to control the excerpt shown on the blog index

Example front matter:

```yaml
---
layout: post
title: "My Post Title"
date: 2026-04-03
categories: waltz
---
```

## YouTube Embeds

The blog supports inline YouTube embeds via `docs/_includes/youtube.html`.

Blog-only shared includes are centralized in `docs/_includes/`, and both local development and GitHub Pages now use the single root config at `docs/_config.yml`.

Posts with embedded YouTube videos should include the `screencast` category in front matter.

Example:

```yaml
categories: waltz screencast
```

Use this in a post:

```liquid
{% include youtube.html id="VIDEO_ID" title="Video Title" %}
```

If the video belongs to a playlist:

```liquid
{% include youtube.html id="VIDEO_ID" list="PLAYLIST_ID" title="Video Title" %}
```

Example:

```liquid
{% include youtube.html id="yL4s0lt51Ts" list="PLGNSioXgrIEfJFJCTFGxKzfoDmxwPEap4" title="Waltz 1.48" %}
```

How to extract the values from a YouTube URL:

- from `watch?v=...`, use the value after `v=` as `id`
- from `&list=...`, use the value after `list=` as `list`

Old thumbnail-link markdown can be replaced with the include above.

## Styling Notes

- The blog uses the `minima` theme with custom overrides in `_sass/minima/custom-styles.scss`
- Tailwind is loaded in `docs/_includes/blog-head-custom.html` for utility classes used in the layouts
- The shell is intentionally aligned with `https://waltz.finos.org/`

## Assets and Paths

- The blog `baseurl` is `/blog`
- Shell images and icons currently reference Waltz assets hosted on `https://waltz.finos.org/site/img/`
- Prefer user-neutral and repo-relative paths in scripts and docs

## Notes

- Browser changes to CSS may require a hard refresh
- Changes to `docs/_config.yml` usually require restarting the Jekyll server
