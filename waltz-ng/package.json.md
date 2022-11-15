# Package JSON docs

## Caution

Upgrading some packages causes problems with the build.
In particular css (fontawesome and ui-grid) gets corrupted.
These problems often only exhibit themselves when a full 
build is deployed, not when using `dev-server`.

The following libs should be upgraded with care:

- `html-webpack-plugin` 4.x seems to break
- `url-loader` 4.x seems to break

