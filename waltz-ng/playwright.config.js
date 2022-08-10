const config = {
    projects: [
        {
            name: "chromium",
            webServer: {
                command: "npm run dev-server",
                url: "http://localhost:8000/",
                timeout: 120 * 1000,
                reuseExistingServer: !process.env.CI,
            },
            reporter: [["html", {open: "always"}]],
            // globalSetup: require.resolve("./playwright-login"),
            use: {
                // trace: "retain-on-failure",
                headless: false,
                browserName: "chromium",
                viewport: {width: 1280, height: 720},
                // ignoreHTTPSErrors: true,
                baseURL: "http://localhost:8000/",
                trace: 'retain-on-failure'
                // storageState: "state.json",
            },
        },
        // {
        //     name: "firefox",
        //     use: {
        //         headless: false,
        //         browserName: "firefox",
        //         viewport: { width: 1280, height: 720 },
        //         ignoreHTTPSErrors: true,
        //     },
        // },
        // {
        //     name: "webkit",
        //     use: {
        //         headless: false,
        //         browserName: "webkit",
        //         viewport: { width: 1280, height: 720 },
        //         ignoreHTTPSErrors: true,
        //     },
        // },
    ],
};

module.exports = config;