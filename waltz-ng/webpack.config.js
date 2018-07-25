var path = require('path');
var webpack = require('webpack');
var HtmlWebpackPlugin = require('html-webpack-plugin');
var Visualizer = require('webpack-visualizer-plugin');
var git = require('git-rev-sync');


var basePath = path.resolve(__dirname);


module.exports = {
    entry: {
        app: './client/main.js'
    },
    output: {
        path: path.join(basePath, '/dist'),
        filename: '[name].js'
    },
    mode: 'development',
    // devtool: 'inline-source-map',
    devtool: 'cheap-module-eval-source-map',
    devServer: {
        contentBase: './dist',
        disableHostCheck: true
    },
    watchOptions: {
        aggregateTimeout: 800,
        poll: 1000
    },
    resolve: {
        symlinks: false
    },
    optimization: {
        splitChunks: {
            chunks: 'all',
            minSize: 30000,
            maxSize: 0,
            minChunks: 1,
            maxAsyncRequests: 5,
            maxInitialRequests: 3,
            automaticNameDelimiter: '~',
            name: true,
            cacheGroups: {
                vendors: {
                    test: /[\\/]node_modules[\\/]/,
                    priority: -10
                },
                default: {
                    minChunks: 2,
                    priority: -20,
                    reuseExistingChunk: true
                }
            }
        }
    },
    plugins: [
        new HtmlWebpackPlugin({
            title: 'Waltz',
            filename: 'index.html',
            template: 'index.ejs',
            favicon: path.join(basePath, 'images', 'favicon.ico'),
            hash: true,
        }),
        new webpack.DefinePlugin({
            '__ENV__': JSON.stringify(process.env.BUILD_ENV || 'dev'),
            '__REVISION__': JSON.stringify(git.long()),
        }),
        new Visualizer()
    ],
    module: {
        rules: [
            {
                test: /\.jsx?$/,
                loader: 'babel-loader',
                exclude: /node_modules/
            }, {
                test: /\.scss$/,
                use: [
                    {
                        loader: 'thread-loader',
                        options: {
                            workerParallelJobs: 2
                        }
                    },
                    'style-loader',
                    'css-loader',
                    'sass-loader']
            }, {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            }, {
                test: /\.(ttf|eot|svg)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
                loader: 'url-loader',
                options: {
                    limit: 8192
                }
            }, {
                test: /\.png$/,
                loader: 'url-loader',
                options: {
                    mimetype: 'image/png',
                    limit: 16384
                }
            }, {
                test: /\.html?$/,
                exclude: /node_modules/,
                loader: 'html-loader'
            }, {
                test: /\.woff(\?v=[0-9]\.[0-9]\.[0-9])?$/,
                loader: 'url-loader',
                options: {
                    mimetype: 'application/font-woff',
                    limit: 8192
                }
            }, {
                test: /\.woff2(\?v=[0-9]\.[0-9]\.[0-9])?$/,
                loader: 'url-loader',
                options: {
                    mimetype: 'application/font-woff2',
                    limit: 8192
                }
            }
        ],
    }

};
