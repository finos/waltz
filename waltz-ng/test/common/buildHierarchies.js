/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */


import assert from "assert";
import { buildHierarchies } from "../../client/common/hierarchy-utils";


const ouA = {
    id: 1
};

const ouA1 = {
    id: 11,
    parentId: 1
};

const ouA2 = {
    id: 12,
    parentId: 1
};

const ouB = {
    id: 2
};

const ouBogusParent = {
    id: 3,
    parentId: -3
};

const ouCycleA = {
    id: 4,
    parentId: 5
};

const ouCycleB = {
    id: 5,
    parentId: 4
};

describe("buildHierarchies", () => {
    it("should give empty array when given no data", () => {
        assert.equal(0, buildHierarchies().length);
    });

    it("should one back if only given one thing", () => {
        assert.equal(1, buildHierarchies([ouA]).length);
    });

    it("gives back an element for each root", () => {
        assert.equal(2, buildHierarchies([ouA, ouB]).length);
    });

    it("builds hierarchies and only returns the roots", () => {
        assert.equal(2, buildHierarchies([ouA, ouA1, ouA2, ouB]).length);
    });

    it("handles bogus parents", () => {
        assert.equal(3, buildHierarchies([ouA, ouB, ouBogusParent]).length);
    });

    it("ignores cycles", () => {
        assert.equal(0, buildHierarchies([ouCycleA, ouCycleB]).length);
    });

});
