import {assert} from "chai";

import store from "../../client/flow-diagram/components/diagram-svelte/store/overlay";
import dirty from "../../client/flow-diagram/components/diagram-svelte/store/dirty";

let currState;
let dirtyFlag;

store.subscribe(d => currState =  d);
dirty.subscribe(d => dirtyFlag =  d);


function reset() {
    store.reset();
    dirty.set(false)
}

describe("flow-diagram/overlay/store", () => {

    it("store and dirty flag can be reset", () => {
        reset();

        const overlay1 = {
            groupRef: "g",
            id: "a",
            kind: "KIND"
        };
        store.addOverlay(overlay1);

        store.setAppliedOverlay("ao");
        store.setSelectedGroup("sg");

        assert.isTrue(dirtyFlag);
        assert.sameDeepMembers(
            currState.groupOverlays.g,
            [{id: "KIND/a", data: overlay1}]);

        assert.equal(currState.appliedOverlay, "ao");
        assert.equal(currState.selectedGroup, "sg");

        reset();

        assert.isFalse(dirtyFlag);
        assert.isEmpty(currState.groupOverlays);
        assert.isNull(currState.appliedOverlay);
        assert.isNull(currState.selectedGroup);
    });

    it("selected group can be set and cleared", () => {
        reset();

        store.setSelectedGroup("sg");
        assert.equal(currState.selectedGroup, "sg");

        store.clearSelectedGroup();
        assert.isNull(currState.selectedGroup);
    });

    it("applied overlay can be set and cleared", () => {
        reset();

        store.setAppliedOverlay("ao");
        assert.equal(currState.appliedOverlay, "ao");

        store.clearAppliedOverlay();
        assert.isNull(currState.appliedOverlay);
    });

    it("overlays are stored by group", () => {
        reset();

        const overlay1 = {
            groupRef: "g",
            id: "a",
            kind: "KIND"
        };
        const overlay2 = {
            groupRef: "g",
            id: "b",
            kind: "KIND"
        };
        store.addOverlay(overlay1);
        store.addOverlay(overlay2);

        assert.isTrue(dirtyFlag);
        assert.sameDeepMembers(
            currState.groupOverlays.g,
            [{id: "KIND/b", data: overlay2}, {id: "KIND/a", data: overlay1}]);
    });

    it("overlays can be overwritten", () => {
        reset();

        const overlay1 = {
            groupRef: "g",
            id: "a",
            kind: "KIND",
            foo: "0"
        };
        const overlay2 = {
            groupRef: "g",
            id: "a",
            kind: "KIND",
            foo: "1"
        };

        store.addOverlay(overlay1)
        store.addOverlay(overlay2);

        assert.deepEqual(currState.groupOverlays.g[0], {id: "KIND/a", data: overlay2});
    });

    it("multiple groups are supported", () => {
        reset();

        const overlay1 = {
            groupRef: "g1",
            id: "a",
            kind: "KIND"
        };
        const overlay2 = {
            groupRef: "g2",
            id: "a",
            kind: "KIND"
        };

        store.addOverlay(overlay1);
        store.addOverlay(overlay2);

        assert.sameDeepMembers(currState.groupOverlays.g1, [{id: "KIND/a", data: overlay1}]);
        assert.sameDeepMembers(currState.groupOverlays.g2, [{id: "KIND/a", data: overlay2}]);
    });

    it("overlays can be removed", () => {
        reset();

        const overlay1 = {
            groupRef: "g1",
            id: "a",
            kind: "KIND"
        };

        store.addOverlay(overlay1);

        assert.sameDeepMembers(currState.groupOverlays.g1, [{id: "KIND/a", data: overlay1}]);

        store.removeOverlay(currState.groupOverlays.g1[0])

        assert.isEmpty(currState.groupOverlays.g1);

    });

    it("existing overlays unaffected by other removals", () => {
        reset();

        const overlay1 = {
            groupRef: "g1",
            id: "a",
            kind: "KIND"
        };

        const overlay2 = {
            groupRef: "g1",
            id: "b",
            kind: "KIND"
        };

        store.addOverlay(overlay1);
        store.addOverlay(overlay2);

        assert.sameDeepMembers(
            currState.groupOverlays.g1,
            [{id: "KIND/a", data: overlay1}, {id: "KIND/b", data: overlay2}]);

        const o1 = currState.groupOverlays.g1[0]
        const o2 = currState.groupOverlays.g1[1]

        store.removeOverlay(o1);
        assert.sameDeepMembers(currState.groupOverlays.g1, [o2]);

        // doesn't matter if we remove it again
        store.removeOverlay(o1);
        assert.sameDeepMembers(currState.groupOverlays.g1, [o2]);
    });

});