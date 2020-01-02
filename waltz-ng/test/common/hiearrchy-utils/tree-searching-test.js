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

import {doSearch, populateParents, prepareSearchNodes} from "../../../client/common/hierarchy-utils";
import {assert} from "chai";
import _ from "lodash";


const a = {
    id: 1,
    name: "Aye",
    description: "Aye desc",
    parentId: null
};


const b = {
    id: 2,
    name: "Bee",
    description: "Bee desc",
    parentId: null
};


const b1 = {
    id: 21,
    name: "BeeOne",
    description: "BeeOne desc",
    parentId: 2
};


const b2 = {
    id: 22,
    name: "BeeTwo",
    description: "BeeTwo desc",
    parentId: 2
};


const b21 = {
    id: 221,
    name: "BeeTwoOne",
    description: "BeeTwoOne desc",
    parentId: 22
};


const nodes = [a, b, b1, b2, b21];
const nodesWithParents = populateParents(nodes, false);
const searchNodes = prepareSearchNodes(nodesWithParents);


describe("HierarchyUtils/treeSearching", () => {
    describe("prepareSearchTerms", () => {
        it("searchNodes should give back an entry for each given node", () => {
            assert.equal(searchNodes.length, nodesWithParents.length, "should have an equal number of inputs and outputs");
        });

        it("searchNodes can have custom search string providers", () => {
            const searchNodes = prepareSearchNodes(nodesWithParents, n => "Silly");
            assert.equal(searchNodes.length, nodesWithParents.length);
            assert.equal(searchNodes[0].searchStr.trim(), "silly", "Expected a fixed searchStr as provider hard-codes 'Silly'");
        });

        it("concatenates parent names to node names and lowercases", () => {
            const expected = _
                .chain([b21, b2, b])
                .map(n => n.name)
                .join(" ")
                .value()
                .toLowerCase();

            const n = _.find(searchNodes, sn => sn.node.id === b21.id);
            assert.equal(n.searchStr.trim(), expected, "Expected search str to be a concat of node + parent node names");
        });
    });

    describe("doSearch", () => {
        it("basic search", () => {
            const hits = doSearch("beeone", searchNodes);
            assert.equal(hits.length, 2);
            assert.equal(hits[0].id, b1.id);
            assert.equal(hits[1].id, b.id)
        });

        it("case insensitive searching is supported", () => {
            const hits = doSearch("BEE TWO ONE", searchNodes);
            assert.equal(hits.length, 3);
            assert.equal(hits[0].id, b21.id);
            assert.equal(hits[1].id, b2.id);
            assert.equal(hits[2].id, b.id)
        });

        it("no hits gives empty array as result", () => {
            const hits = doSearch("FOO", searchNodes);
            assert.equal(hits.length, 0);
        });

        it("no search nodes gives back empty array", () => {
            const hits = doSearch("FOO", []);
            assert.equal(hits.length, 0);
        });
    });
});
