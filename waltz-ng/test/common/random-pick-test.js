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
import {notEmpty, randomPick} from "../../client/common";
import {tryOrDefault} from "../../client/common/function-utils";



describe("common/function-utils", () => {

    describe("notEmpty", () => {

        it("returns true if an array or string is not empty", () => {
            assert(notEmpty([1]));
            assert(notEmpty("hello"));
        });

        it("returns false if an array or string is empty", () => {
            assert.isFalse(notEmpty([]));
            assert.isFalse(notEmpty(""));
            assert.isFalse(notEmpty(null));
        });
    });

    describe("tryOrDefault", () => {

        it("gives the result of calling `fn()` as long as `fn` does not throw an error", () => {
            assert.equal(4, tryOrDefault(() => 2 + 2, -1));
        });

        it("gives the default value if  `fn()` throws an error", () => {
            assert.equal(-1, tryOrDefault(() => { throw "bang"; }, -1));
        });

    });

    describe("randomPick", () => {

        it("returns the only element of a one element array", () => {
            assert.equal(3, randomPick([3]));
        });

        it("returns null from a zero element array", () => {
            assert.equal(null, randomPick([]));
        });

        it("throws with a null array", () => {
            assert.throws(() => randomPick());
        });

    });

});
