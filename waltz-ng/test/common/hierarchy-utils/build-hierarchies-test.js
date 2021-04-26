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


import {assert} from "chai";
import { buildHierarchies } from "../../../client/common/hierarchy-utils";


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

describe("HierarchyUtils/buildHierarchies", () => {
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
