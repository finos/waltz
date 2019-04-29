import {updateDirtyFlags } from "../../client/allocation/allocation-utilities";
import {calcWorkingTotal } from "../../client/allocation/allocation-utilities";
import {updateFloatingValues } from "../../client/allocation/allocation-utilities";
import {assert} from "chai"
import _ from "lodash";

describe("AllocationUtils", () => {

    describe("updateDirtyFlags", () => {
        it("if percentages differ then item is dirty", () => {
            const hasDiff = { allocation: { percentage: 20}, working: { percentage: 10, }};
            const noDiff = { allocation: { percentage: 20}, working: { percentage: 20, }};

            updateDirtyFlags([hasDiff, noDiff]);

            assert(hasDiff.working.dirty);
            assert(!noDiff.working.dirty);
        });

        it("if item newly allocated then item is dirty", () => {
            const newAlloc = { allocation: null, working: { isAllocated: true, }};
            const existingAlloc = { allocation: { foo: "baa "}, working: { isAllocated: true }};

            updateDirtyFlags([newAlloc, existingAlloc]);

            assert.isTrue(newAlloc.working.dirty);
            assert.isFalse(existingAlloc.working.dirty);
        });

        it("if item newly unallocated then item is dirty", () => {
            const newUnalloc = { allocation: {foo: "baa" }, working: { isAllocated: false, }};
            const existingAlloc = { allocation: { foo: "baa "}, working: { isAllocated: true }};

            updateDirtyFlags([newUnalloc, existingAlloc]);

            assert.isTrue(newUnalloc.working.dirty);
            assert.isFalse(existingAlloc.working.dirty);
        });
    });


    describe("calcWorkingTotals", (enrichedAllocations = []) => {
        it("workingTotals has zero allocations, calculated total equals zero", () => {

            const enrichedAllocations = [];

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

});
