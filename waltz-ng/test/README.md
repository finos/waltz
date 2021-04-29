# JS Unit Tests

Tests are written using [Mocha](https://mochajs.org/) in a BDD style.
We use the [Chai Assertion library](https://www.chaijs.com/api/assert/) for verifying
behaviour.


## Running Tests

### From command line

Simple run:

    $> cd <repos>/waltz/walt-ng
    $> npm run test  
    
### From IntelliJ

Ensure Node support is installed.  Configure a test Mocha test runner with 
options similar to the following:

- mocha package: `<repos>/waltz/waltz-ng/node_modules/mocha`
- mocha options: `--recursive --require @babel/register`
- test directory: `<repos>/waltz/waltz-ng/tests`
- working directory: `<repos>/waltz`
- interface style: `bdd`
