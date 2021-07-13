import {assert} from "chai";

import {positions} from "../../client/flow-diagram/components/diagram-svelte/store/layout";

let currState;
positions.subscribe(d => currState =  d);


describe("flow-diagram/layout/store", () => {

    it("can position absolutely or releatively (setPosition vs move)", () => {
        positions.setPosition({id: "t", x: 10, y: 20});
        assert.deepEqual(currState.t, {x: 10, y: 20});

        positions.setPosition({id: "t", x: 20, y: 20});
        assert.deepEqual(currState.t, {x: 20, y: 20});

        positions.move({id: "t", dx: 50});
        assert.deepEqual(currState.t, {x: 70, y: 20});

        positions.move({id: "t", dy: 30});
        assert.deepEqual(currState.t, {x: 70, y: 50});

        positions.setPosition({id: "t", x: 0, y: 20});
        assert.deepEqual(currState.t, {x: 0, y: 20});
    });

    it("can position multiple items", () => {
        positions.setPosition({id: "a", x: 10, y: 20});
        positions.setPosition({id: "b", x: 30, y: 40});
        positions.setPosition({id: "c", x: 50, y: 60});

        assert.deepEqual(currState.a, {x: 10, y: 20});
        assert.deepEqual(currState.b, {x: 30, y: 40});
        assert.deepEqual(currState.c, {x: 50, y: 60});

        positions.move({id: "a", dx: -10, dy: -30});
        positions.move({id: "b", dx: 30});
        positions.move({id: "c", dy: 30});

        assert.deepEqual(currState.a, {x: 0, y: -10});
        assert.deepEqual(currState.b, {x: 60, y: 40});
        assert.deepEqual(currState.c, {x: 50, y: 90});
    });

    it("can be reset, clearing all positions", () => {
        positions.setPosition({id: "a", x: 10, y: 20});
        positions.setPosition({id: "b", x: 30, y: 40});

        assert.deepEqual(currState.a, {x: 10, y: 20});
        assert.deepEqual(currState.b, {x: 30, y: 40});

        positions.reset();

        assert.isEmpty(currState);
    });

    it("can be reset, clearing all positions", () => {
        positions.setPosition({id: "a", x: 10, y: 20});
        positions.setPosition({id: "b", x: 30, y: 40});

        assert.deepEqual(currState.a, {x: 10, y: 20});
        assert.deepEqual(currState.b, {x: 30, y: 40});

        positions.reset();

        assert.isEmpty(currState);
    });

    it("falls back to using refId as starting position if the id isn't already known", () => {
        positions.setPosition({id: "a", x: 10, y: 20});
        assert.deepEqual(currState.a, {x: 10, y: 20});

        positions.move({id: "b", refId: "a", dy: 30});
        assert.deepEqual(currState.b, {x: 10, y: 50});
    });
});