import {doSearch, populateParents, prepareSearchNodes} from "../../../client/common/hierarchy-utils";
import assert from "assert";
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


describe("prepareSearchTerms", () => {
    it("searchNodes should give back an entry for each given node", () => {
        assert.equal(searchNodes.length, nodesWithParents.length, "should have an equal number of inputs and outputs");
    });

    it("searchNodes can have custom search string providers", () => {
        const searchNodes = prepareSearchNodes(nodesWithParents, n => "Silly");
        assert(searchNodes.length, nodesWithParents.length);
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
        assert(hits.length === 2);
        assert(hits[0].id === b1.id);
        assert(hits[1].id === b.id)
    });

    it("case insensitive searching is supported", () => {
        const hits = doSearch("BEE TWO ONE", searchNodes);
        assert(hits.length === 3);
        assert(hits[0].id === b21.id);
        assert(hits[1].id === b2.id);
        assert(hits[2].id === b.id)
    });

    it("no hits gives empty array as result", () => {
        const hits = doSearch("FOO", searchNodes);
        assert(hits.length === 0);
    });

    it("no search nodes gives back empty array", () => {
        const hits = doSearch("FOO", []);
        assert(hits.length === 0);
    });
});


