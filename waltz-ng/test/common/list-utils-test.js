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
import * as lu from "../../client/common/list-utils";
import {move} from "../../client/common/list-utils";


const abc = ["a", "b", "c"];
const abcd = ["a", "b", "c", "d"];

describe("ListUtils", () => {
    describe("containsAll", () => {
        it ("returns true if all items in the second list are contained in the first",
            () => assert.isTrue(lu.containsAll(abc, abc)));

        it ("true if the first is a superset",
            () => assert.isTrue(lu.containsAll(abcd, abc)));

        it ("true if the second is empty",
            () => assert.isTrue(lu.containsAll(abcd, [])));

        it ("true if the second is null",
            () => assert.isTrue(lu.containsAll(abcd, null)));

        it ("true if both are empty",
            () => assert.isTrue(lu.containsAll([], [])));

        it ("true if both are null",
            () => assert.isTrue(lu.containsAll(null, null)));

        it ("false if second contains things first doesn't have",
            () => assert.isFalse(lu.containsAll(abc, abcd)));
    });

    describe("cumulativeCounts", () => {
        it ("returns [] if given counts of [0]",
            () => assert.deepEqual(lu.toCumulativeCounts([]), []));
        it ("returns [] if given counts of -nothing-",
            () => assert.deepEqual(lu.toCumulativeCounts(), []));
        it ("returns [] if given counts of -null-",
            () => assert.deepEqual(lu.toCumulativeCounts(null), []));
        it ("returns [1] if given counts of [1]",
            () => assert.deepEqual(lu.toCumulativeCounts([1]), [1]));
        it ("returns [8] if given counts of [8]",
            () => assert.deepEqual(lu.toCumulativeCounts([8]), [8]));
        it ("returns [1,4] if given counts of [1,3]",
            () => assert.deepEqual(lu.toCumulativeCounts([1, 3]), [1, 4]));
        it ("returns [1,4,6] if given counts of [1,3,2]",
            () => assert.deepEqual(lu.toCumulativeCounts([1, 3, 2]), [1, 4, 6]));
    });

    describe("toOffsetMap", () => {
        it ("gives an offset map into an array of items",
            () => {
                const offsets = lu.toOffsetMap(["a", "b", "c"], x => x);
                assert.equal(offsets.a, 0);
                assert.equal(offsets.b, 1);
                assert.equal(offsets.c, 2);
                assert.equal(offsets.d, null);
            });
        it ("gives an empty map if given no items",
            () => assert.deepEqual(lu.toOffsetMap([]), {}));
        it ("gives an empty map if given -nothing-",
            () => assert.deepEqual(lu.toOffsetMap(), {}));
        it ("gives an empty map if given -null-",
            () => assert.deepEqual(lu.toOffsetMap(null), {}));
    });


    describe("move", () => {
        const arr = ["a", "b", "c"];

        it("will leave an item in place if delta == 0", () => {
            assert.deepEqual(move(arr, 1, 0), ["a", "b", "c"]);
        });

        it("can move an element up (delta = +1)", () => {
            assert.deepEqual(move(arr, 0, 1), ["b", "a", "c"]);
            assert.deepEqual(move(arr, 1, 1), ["a", "c", "b"]);
        });

        it("can move an element down (delta = -1)", () => {
            assert.deepEqual(move(arr, 1, -1), ["b", "a", "c"]);
            assert.deepEqual(move(arr, 2, -1), ["a", "c", "b"]);
        });

        it("can move an element several places", () => {
            assert.deepEqual(move(arr, 2, -2), ["c", "a", "b"]);
            assert.deepEqual(move(arr, 0, 2), ["b", "c", "a"]);
        });

        it("clamps to end", () => {
            assert.deepEqual(move(arr, 2, 10), ["a", "b", "c"]);
            assert.deepEqual(move(arr, 1, 10), ["a", "c", "b"]);
        });

        it("clamps to beginning", () => {
            assert.deepEqual(move(arr, 0, -10), ["a", "b", "c"]);
            assert.deepEqual(move(arr, 1, -10), ["b", "a", "c"]);
            assert.deepEqual(move(arr, 2, -10), ["c", "a", "b"]);
        });
    });


    describe("mkChunks", () => {
        it("works for empty arrays", () => {
            assert.deepEqual(lu.mkChunks([], 2), []);
        });

        it("works for undefined arrays", () => {
            assert.deepEqual(lu.mkChunks(undefined, 2), []);
        });

        it("works for equal chunks", () => {
            assert.deepEqual(lu.mkChunks(abcd, 2), [["a", "b"], ["c", "d"]]);
        });

        it("works for unequal chunks", () => {
            assert.deepEqual(lu.mkChunks(abc, 2), [["a", "b"], ["c"]]);
        });

        it("works for chunk size equal to original array size", () => {
            assert.deepEqual(lu.mkChunks(abc, 3), [["a", "b", "c"]]);
        });

        it("works for chunk size greater than original array size", () => {
            assert.deepEqual(lu.mkChunks(abc, 4), [["a", "b", "c"]]);
        });
    });
});

