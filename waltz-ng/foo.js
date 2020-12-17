const mode = process.env.NODE_ENV || "development";
const prod = mode === "production";

module.exports = {
    entry: {
        bundle: [
            "@webcomponents/custom-elements",
            "./src/main.js"
        ]
    },
    resolve: {
        extensions: [".mjs", ".js", ".svelte", ".css"]
    },
    output: {
        path: __dirname + "/public",
        filename: "[name].js",
        chunkFilename: "[name].[id].js"
    },
    module: {
        rules: [
            {
                test: /(\.m?js?$)|(\.svelte$)/,
                exclude: /\bcore-js\b/,
                use: {
                    loader: "babel-loader",
                    options: {
                        presets: [
                            ["@babel/preset-env", {
                                targets: {
                                    "browsers": [
                                        "ie >= 10"
                                    ]
                                },
                                useBuiltIns: "usage",
                                corejs: 3
                            }]
                        ],
                        plugins: [
                            // '@babel/plugin-proposal-class-properties',
                            // '@babel/plugin-transform-shorthand-properties'

                        ],
                        sourceType: "unambiguous"
                    }
                }
            },
            {
                test: /\.svelte$/,
                exclude: /node_modules/,
                use: {
                    loader: "svelte-loader",
                    options: {
                        emitCss: true,
                        hotReload: true
                    }
                }
            },
            {
                test: /\.css$/,
                use: [
                    /**
                     * MiniCssExtractPlugin doesn't support HMR.
                     * For developing, use 'style-loader' instead.
                     * */
                    prod ? MiniCssExtractPlugin.loader : "style-loader",
                    "css-loader"
                ]
            }
        ]
    },
    mode,
    plugins: [
        new MiniCssExtractPlugin({
            filename: "[name].css"
        })
    ],
    devtool: prod ? false : "source-map"
};