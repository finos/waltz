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
const SpeedMeasurePlugin = require("speed-measure-webpack-plugin");

const smp = new SpeedMeasurePlugin();
const {merge} = require("webpack-merge");
const common = require("./webpack.config");

module.exports = smp.wrap(merge(common, {
    mode: "development",
    //devtool: "inline-source-map",
    //devtool: "cheap-module-eval-source-map",
    devServer: {
        static: "./dist", //contentBase: "./dist",
        allowedHosts: "all", //disableHostCheck: true,
        historyApiFallback: {
            disableDotRule: true
        },
        proxy: [{
            context: ["/data-extract", "/api", "/authentication"],
            target: "http://[::1]:8443", // see note [1]
        }]
    }
}));

/*
[1] - accessing the server using localhost in a browser seems to incur a significant proxy performance cost
      this can be mitigated by using loopback address (seen here specified in ipv6 form)
 */