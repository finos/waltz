# Playwright Tests

## Overview

We are experimenting with using Playwright for end-to-end browser based tests.

Currently, the tests are running but need a backend spun up on port 8000  (see `BasePlaywrightTest`).

## Running

The tests in this package (`org.finos.waltz.test_common.playwright`) can be run manually, or triggered as part of a maven build by including the `playwright-tests` profile (e.g. `mvn -Pplaywright-tests,...`).

## Authoring tests

Each test class should prepare data using the code provided in the `src/main` part of this sub-project.  New tests should extend `BasePlaywrightIntegrationTest`. 

If you need to add custom attributes to the dom to help with writing locators use: `data-testid`.  See [`Locator::getByTestId`](https://playwright.dev/java/docs/api/class-locator#locator-get-by-test-id) for more info. 

