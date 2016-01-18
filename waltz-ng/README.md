# Web Client

## Prerequisites

* Node/npm
    * Global node packages
        * Webpack
        * Webpack-dev-server
        * Mocha (testing)
        * bower
        * gulp



## Building the client app

    $> cd ./waltz-ng/
    $> npm install             -- takes some time
    $> npm run dev-server

## Development

Leave dev processes running in the background.  It will compile and re-bundle
as needed.  It will also generate css from the sass config (in `style`)

For Testing run mocha either from your IDE on demand or in watch mode from your
shell with:

    $> cd ./waltz-ng/
    $> mocha --recursive --watch
