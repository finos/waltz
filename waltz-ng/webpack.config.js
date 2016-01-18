var path = require('path');
var webpack = require('webpack');
var HtmlWebpackPlugin = require('html-webpack-plugin');

var basePath = path.resolve(__dirname);
var nodeModulesDir = path.resolve(__dirname, 'node_modules');

module.exports = {
    entry: './client/main.js',
    devtool: 'cheap-source-map',
    output: {
        path: path.join(basePath, '/dist'),
        filename: 'bundle.js'
    },
    plugins: [
        new HtmlWebpackPlugin({
            title: 'Waltz',
            filename: 'index.html',
            template: 'index.template.html',
            favicon: path.join(basePath, 'images', 'favicon.ico')
        }),
        new webpack.DefinePlugin({
            __ENV__: JSON.stringify(process.env.BUILD_ENV || 'dev')
        })
    ],
    module: {
        loaders: [
            { test: /\.jsx?$/, loader: 'babel', exclude: /node_modules/ },
            { test: /\.scss$/, loader: 'style!css!sass' },
            { test: /\.css$/, loader: 'style!css' },
            { test: /\.(ttf|eot|svg)(\?v=[0-9]\.[0-9]\.[0-9])?$/, loader: 'file-loader' },
            { test: /\.html?$/, loader: 'raw-loader' },
            { test: /\.woff(\?v=[0-9]\.[0-9]\.[0-9])?$/, loader: 'url-loader?limit=10000&minetype=application/font-woff' },
            { test: /\.woff2(\?v=[0-9]\.[0-9]\.[0-9])?$/, loader: 'url-loader?limit=10000&minetype=application/font-woff2' }
        ],
        noParse: []
    }

};
