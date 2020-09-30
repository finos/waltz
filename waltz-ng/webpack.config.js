/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

const { CleanWebpackPlugin } = require("clean-webpack-plugin");
const git = require("git-rev-sync");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const path = require("path");
const webpack = require("webpack");

const basePath = path.resolve(__dirname);

module.exports = {
    entry: {
        app: "./client/main.js"
    },
    output: {
        path: path.join(basePath, "/dist"),
        filename: "[name].[contenthash].js"
    },
    resolve: {
        symlinks: false
    },
    optimization: {
        runtimeChunk: "single",
        splitChunks: {
            chunks: "all",
            minSize: 30000,
            maxSize: 600000,
            minChunks: 1,
            maxAsyncRequests: 8,
            maxInitialRequests: 4,
            automaticNameDelimiter: "~",
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
    watchOptions: {
        ignored: /node_modules/,
        aggregateTimeout: 800
    },
    plugins: [
        new CleanWebpackPlugin(),
        new HtmlWebpackPlugin({
            title: "Waltz",
            filename: "index.html",
            template: "index.ejs",
            favicon: path.join(basePath, "images", "favicon.ico"),
            hash: true,
        }),
        new webpack.DefinePlugin({
            "__ENV__": JSON.stringify(process.env.BUILD_ENV || "dev"),
            "__REVISION__": JSON.stringify(git.long()),
        })
    ],
    module: {
        rules: [
            {
                test: /\.jsx?$/,
                use: ["babel-loader"],
                exclude: /node_modules/
            }, {
                test: /\.scss$/,
                use: [
                    {
                        loader: "thread-loader",
                        options: {
                            workerParallelJobs: 2
                        }
                    },
                    "style-loader",
                    "css-loader",
                    "sass-loader"]
            }, {
                test: /\.css$/,
                use: ["style-loader", "css-loader"]
            }, {
                test: /\.(ttf|eot|svg|woff(2)?)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
                loader: "url-loader",
                options: {
                    limit: 8192
                }
            }, {
                test: /\.png$/,
                loader: "file-loader",
                options: {
                    mimetype: "image/png",
                    limit: 16384
                }
            }, {
                test: /\.html?$/,
                exclude: /node_modules/,
                loader: "html-loader"
            }
        ],
    }
};
