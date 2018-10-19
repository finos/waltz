const merge = require("webpack-merge");
const common = require("./webpack.config");

module.exports = merge(common, {
    mode: "development",
    //devtool: "inline-source-map",
    devtool: "cheap-module-eval-source-map",
    devServer: {
        contentBase: "./dist",
        disableHostCheck: true
    }
});