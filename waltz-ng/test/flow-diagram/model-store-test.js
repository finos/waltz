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
    a: {id: "n1"},
    dupeOfA: {id: "n1"},
    b: {id: "n2"}
};

const fs = {
    a: {id: "f1"},
    dupeOfA: {id: "f1"},
    b: {id: "f2"},
};

const as = {
    a: {id: "a1"},
    dupeOfA: {id: "a1"},
    b: {id: "a2"}
};


function testEntityAdd(addFn, propFn, entitySet) {

    it("throws error if adding a null entity", () => {
        reset();
        assert.throw(() => addFn(null));
    });

    it("throws error if adding an entity without an id", () => {
        reset();
        assert.throw(() => addFn({missingId: "lol"}));
    });

    it("store and dirty flag can be reset", () => {
        reset();

        addFn(entitySet.a);

        assert.sameDeepMembers(propFn(), [entitySet.a]);
        assert.isTrue(dirtyFlag);

        reset();

        assert.isEmpty(propFn());
        assert.isFalse(dirtyFlag);
    });

    it("can handle many entities, ignoring dupes (by id)", () => {
        reset();

        addFn(entitySet.a);
        addFn(entitySet.dupeOfA);
        addFn(entitySet.b);

        assert.sameDeepMembers(propFn(), [entitySet.a, entitySet.b]);
        assert.isTrue(dirtyFlag);
    });
}


describe("flow-diagram/model/store", () => {
    describe("addNode", () => testEntityAdd(store.addNode, () => currState.nodes, ns));
    describe("addFlow", () => testEntityAdd(store.addFlow, () => currState.flows, fs));
    describe("addAnnotations", () => testEntityAdd(store.addAnnotation, () => currState.annotations, as));
});