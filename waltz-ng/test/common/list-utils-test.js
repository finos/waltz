import assert from "assert";
import * as lu from "../../client/common/list-utils";


const abc = ["a", "b", "c"];
const abcd = ["a", "b", "c", "d"];


describe("containsAll", () => {
    it ("returns true if all items in the second list are contained in the first",
        () => assert(lu.containsAll(abc, abc)));

    it ("true if the first is a superset",
        () => assert(lu.containsAll(abcd, abc)));

    it ("true if the second is empty",
        () => assert(lu.containsAll(abcd, [])));

    it ("true if the second is null",
        () => assert(lu.containsAll(abcd, null)));

    it ("true if both are empty",
        () => assert(lu.containsAll([], [])));

    it ("true if both are null",
        () => assert(lu.containsAll(null, null)));

    it ("false if second contains things first doesn't have",
        () => assert(! lu.containsAll(abc, abcd)));
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
            assert.equal(offsets["a"], 0);
            assert.equal(offsets["b"], 1);
            assert.equal(offsets["c"], 2);
            assert.equal(offsets["d"], null);
        });
    it ("gives an empty map if given no items",
        () => assert.deepEqual(lu.toOffsetMap([]), []));
    it ("gives an empty map if given -nothing-",
        () => assert.deepEqual(lu.toOffsetMap(), []));
    it ("gives an empty map if given -null-",
        () => assert.deepEqual(lu.toOffsetMap(null), []));
});



