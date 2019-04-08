import {updateDirtyFlags } from "../../client/allocation/allocation-utilities";
import {calcWorkingTotal } from "../../client/allocation/allocation-utilities";
import assert from "assert";

describe("updateDirtyFlags", () => {
    it("if percentages differ then item is dirty", () => {

        const hasDiff = { allocation: { percentage: 20}, working: { percentage: 10, }};
        const noDiff = { allocation: { percentage: 20}, working: { percentage: 20, }};
        updateDirtyFlags([hasDiff, noDiff]);

        assert(hasDiff.working.dirty);
        assert(!noDiff.working.dirty);
    });

    it("if types differ then item is dirty", () => {

        const hasDiff = { allocation: { type: "FIXED"}, working: { type: "FLOATING", }};
        const noDiff = { allocation: { type: "FIXED"}, working: { type: "FIXED", }};
        updateDirtyFlags([hasDiff, noDiff]);

        assert(hasDiff.working.dirty);
        assert(!noDiff.working.dirty);
    });
});


describe("calcWorkingTotals", (enrichedAllocations = []) => {
    it("workingTotals has zero allocations, calculated total equals zero", () => {

        const enrichedAllocations = []

        assert.equal(0, calcWorkingTotal(enrichedAllocations));
    });
    it("allocation with no percentage contribute zero", () => {

        const enrichedAllocations = [{working: { percentage:0}}, {working: { foo: 67 }}, { haha: 7}];

        assert.equal(0, calcWorkingTotal(enrichedAllocations));
    });
    it("can add allocation values correctly", () => {

        const p50 = { working: { percentage: 50 }};
        const p30 = { working: { percentage: 30 }};
        const p20 = { working: { percentage: 20 }};
        const p10 = { working: { percentage: 10 }};
        const p0  = { working: { percentage: 0 }};

        assert.equal(100, calcWorkingTotal([ p50, p30, p20 ]));
        assert.equal(20, calcWorkingTotal([ p10, p10, p0 ]));
        assert.equal(0,  calcWorkingTotal([ p0 ]));
        assert.equal(10, calcWorkingTotal([ p0, p10 ]));

    });
});

