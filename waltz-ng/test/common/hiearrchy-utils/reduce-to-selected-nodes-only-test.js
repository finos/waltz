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
import {populateParents, reduceToSelectedNodesOnly} from "../../../client/common/hierarchy-utils";


const nA = {
    id: 1
};

const nA1 = {
    id: 11,
    parentId: nA.id
};

const nA2 = {
    id: 12,
    parentId: nA.id
};

const nB = {
    id: 2
};

const nB1 = {
    id: 21,
    parentId: nB.id
};

const nC = {
    id: 3
};

const nC1 = {
    id: 31,
    parentId: nC.id
};

const nC11 = {
    id: 311,
    parentId: nC1.id
};

const allNodes = [nA, nA1, nA2, nB, nB1, nC, nC1, nC11];

const hierarchy = populateParents(allNodes);


describe("HierarchyUtils/reduceToSelectedNodesOnly", () => {
    it("gives back child and their parents", () => {
        assert.deepEqual(
            [nA.id, nA2.id],
            reduceToSelectedNodesOnly(hierarchy, [nA2.id])
                .map(d => d.id)
                .sort());
    });

    it("if given top nodes then only gives back those", () => {
        assert.deepEqual(
            [nA.id],
            reduceToSelectedNodesOnly(hierarchy, [nA.id])
                .map(d => d.id)
                .sort());
    });

});
