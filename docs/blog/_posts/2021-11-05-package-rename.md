---
layout: post
title:  "Development: Renaming Packages"
date:   2021-11-04
categories: waltz dev
---

# Moving into the `org.finos` namespace

As part of the development of Waltz 1.39 we have (finally) completed the renaming of Waltz assets away from `com.khartec`
and onto `org.finos`.

## What's changed

From an end-user point of view **nothing will look different**.

However, for developers they will notice that:

- all packages have had their prefixes renamed
- the Maven groups has changed to be `org.finos`
- generated code goes into an `org.finos` package structure
- ArchUnit tests have been updated to account for the new package structure

We have taken care to preserve git history so using tools like `git blame` will still function as expected.

Changes that may be needed are:

- launchers, may need their fully qualified class names updating (simply replace `com.khartec` with `org.finos`).

## Help

We've tested this change extensively and don't believe it will cause any problems.
If you do find some issues please reach out to us and we will do our best to help you resolve them.

