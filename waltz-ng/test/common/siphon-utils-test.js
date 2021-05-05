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

import {assert} from "chai";
import _ from "lodash";
import {mkSiphon} from "../../client/common/siphon-utils";

const data = [1, 2, 3];

describe("common/siphon-utils", () => {
    describe("mkSiphon", () => {
        it ("throws error if not given a predicate",
            () => assert.throws(() => mkSiphon(null)));
        it ("throws error if not given a function as a predicate",
            () => assert.throws(() => mkSiphon(2)));
        it ("when first created has empty results",
            () => assert.isEmpty(mkSiphon(() => true).results));
        it ("siphons everything that passes the predicate (scenario: siphon everything)",
            () => {
            
                const siphon = mkSiphon(() => true);
                const keptResults = _.reject(data, siphon);


                assert.isEmpty(keptResults, "siphon should have caught everything")
                assert.isTrue(siphon.hasResults(), "siphon should have results")
                assert.deepEqual([1, 2, 3], siphon.results)
            });
        it ("siphons everything that passes the predicate (scenario: siphons nothing)",
            () => {

                const siphon = mkSiphon(() => false);
                const keptResults = _.reject(data, siphon);


                assert.isFalse(siphon.hasResults(), "siphon should be empty")
                assert.deepEqual([1, 2, 3], keptResults);
            });
        it ("siphons everything that passes the predicate (scenario: siphons even numbers)",
            () => {

                const siphon = mkSiphon((d) => d % 2 === 0);
                const keptResults = _.reject(data, siphon);

                assert.isTrue(siphon.hasResults(), "siphon should have results");
                assert.deepEqual([2], siphon.results, "expected even numbers to be caught by siphon");
                assert.deepEqual([1, 3], keptResults, "expected only odd numbers to be kept");
            });
    });
});



