import { defineConfig, devices } from "@playwright/test";

/**
 * Targets a running Waltz instance via WALTZ_BASE_URL (defaults to the local smoke stack on
 * :8080). In the containerised model this points at the app container's mapped port.
 */
const baseURL = process.env.WALTZ_BASE_URL ?? "http://localhost:8080";

export default defineConfig({
    testDir: "./tests",
    timeout: 45_000,
    // Generous auto-wait: the suite drives a single, freshly-started (cold) app instance.
    expect: { timeout: 15_000 },
    // No retries — CI must fail on real flakiness rather than mask it. Tests must be deterministic.
    retries: 0,
    // Cap concurrency so the single app instance isn't overwhelmed (keeps waits honest).
    workers: process.env.CI ? 2 : 4,
    // Evidence written to playwright-report/ after every run:
    //  - list : per-test pass/fail text on stdout (CI logs)
    //  - html : self-contained report with a screenshot + trace per test
    //  - junit: machine-readable text report (results.xml) for CI tooling
    reporter: [
        ["list"],
        ["html", { open: "never", outputFolder: "playwright-report" }],
        ["junit", { outputFile: "playwright-report/results.xml" }]
    ],
    use: {
        baseURL,
        headless: true,
        trace: "on",
        screenshot: "on",
        video: "retain-on-failure"
    },
    projects: [
        { name: "chromium", use: { ...devices["Desktop Chrome"] } }
    ]
});
