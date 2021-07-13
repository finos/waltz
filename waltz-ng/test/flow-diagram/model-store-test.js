import {assert} from "chai";

import store from "../../client/flow-diagram/components/diagram-svelte/store/model";
import dirty from "../../client/flow-diagram/components/diagram-svelte/store/dirty";

let currState;
let dirtyFlag;

store.subscribe(d => currState =  d);
dirty.subscribe(d => dirtyFlag =  d);


function reset() {
    store.reset();
    dirty.set(false)
}

const ns = {
    a: {id: "APPLICATION/1", data: {id: 1, kind: "APPLICATION"}},
    dupeOfA: {id: "APPLICATION/1", data: {id: 1, kind: "APPLICATION"}},
    b: {id: "APPLICATION/2", data: {id: 2, kind: "APPLICATION"}}
};

const fs = {
    a: {id: "LOGICAL_FLOW/1", data: {id: 1, kind: "LOGICAL_FLOW", source: ns.a.data, target: ns.b.data}},
    dupeOfA: {id: "LOGICAL_FLOW/1"},
    b: {id: "LOGICAL_FLOW/2"},
};

const as = {
    a: {id: "a1", data: { entityReference: ns.a.data}},
    dupeOfA: {id: "a1", data: { entityReference: ns.a.data}},
    b: {id: "a2", data: {entityReference: fs.a.data}}
};

const rs = {
    a: {id: "r1"},
    dupeOfA: {id: "r1"},
    b: {id: "r2"}
};


function testEntityAddAndRemove(addFn, removeFn, propFn, entitySet) {

    it("throws error if adding a null entity", () => {
        assert.throw(() => addFn(null));
    });

    it("throws error if adding an entity without an id", () => {
        reset();
        assert.throw(() => addFn({missingId: "lol"}));
    });

    it("store and dirty flag can be reset", () => {
        addFn(entitySet.a);

        assert.sameDeepMembers(propFn(), [entitySet.a]);
        assert.isTrue(dirtyFlag);

        reset();

        assert.isEmpty(propFn());
        assert.isFalse(dirtyFlag);
    });

    it("can handle many entities, ignoring dupes (by id)", () => {
        addFn(entitySet.a);
        addFn(entitySet.dupeOfA);
        addFn(entitySet.b);

        assert.sameDeepMembers(propFn(), [entitySet.a, entitySet.b]);
        assert.isTrue(dirtyFlag);
    });

    it("can remove an item", () => {
        addFn(entitySet.a);
        assert.sameDeepMembers(propFn(), [entitySet.a]);
        removeFn(entitySet.a)
        assert.isEmpty(propFn());
    });

    it("will leave unremoved items alone", () => {
        addFn(entitySet.a);
        addFn(entitySet.b);
        assert.sameDeepMembers(propFn(), [entitySet.a, entitySet.b]);
        removeFn(entitySet.a)
        assert.sameDeepMembers(propFn(), [entitySet.b]);
    });
}


describe("flow-diagram/model/store", () => {
    beforeEach(() => reset());
    describe("nodes", () => {
        describe("add/remove", () => testEntityAddAndRemove(store.addNode, store.removeNode,() => currState.nodes, ns));

        describe("cascade delete", () => {
            it("removes annotations attached to nodes", () => {
                store.addNode(ns.a);
                store.addAnnotation(as.a);

                store.removeNode(ns.a);

                assert.isEmpty(currState.nodes)
                assert.isEmpty(currState.annotations)
            });

            it("removes flows involving the node to be removed", () => {
                store.addNode(ns.a);
                store.addNode(ns.b);
                store.addFlow(fs.a);

                store.removeNode(ns.a);

                assert.sameDeepMembers(currState.nodes, [ns.b]);
                assert.isEmpty(currState.flows)
            });
        });
    });

    describe("flows", () => {
        describe("add/remove", () => testEntityAddAndRemove(store.addFlow, store.removeFlow, () => currState.flows, fs));

        describe("cascade delete", () => {
            it("removes annotations attached to flows", () => {
                store.addNode(ns.a);
                store.addNode(ns.b);
                store.addFlow(fs.a);
                store.addAnnotation(as.b);

                store.removeFlow(fs.a);

                assert.isEmpty(currState.annotations)
                assert.isEmpty(currState.flows);

                // nodes should remain unaffected by flow removal
                assert.sameDeepMembers(currState.nodes, [ns.a, ns.b]);
            });
        });
    });

    describe("annotations", () => {
        describe("add/remove", () => testEntityAddAndRemove(store.addAnnotation, store.removeAnnotation, () => currState.annotations, as));
    });

    describe("relationships", () => {
        describe("add/remove", () => testEntityAddAndRemove(store.addRelationship, store.removeRelationship,() => currState.relationships, rs));
    });
});