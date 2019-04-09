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

        it("if types differ then item is dirty", () => {

            const hasDiff = { allocation: { type: "FIXED"}, working: { type: "FLOATING", }};
            const noDiff = { allocation: { type: "FIXED"}, working: { type: "FIXED", }};
            updateDirtyFlags([hasDiff, noDiff]);

            assert.isTrue(hasDiff.working.dirty);
            assert.isFalse(noDiff.working.dirty);
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


    describe("updateFloatingValues", () => {

        const assertAllocation = (allocs = [], idx = 0, percentage = 0) => {
            assert.equal(_.get(allocs[idx], ["working", "percentage"]), percentage);
        };

        it("when given zero floating allocations, zero are returned", () => {
            assert.deepEqual([], updateFloatingValues(0, []));
        });

        it("when given an allocation and no floatTotal allocation will be zero", () => {
            const updatedAllocations = updateFloatingValues(0, [ { working: { percentage: 50 }}]);
            assertAllocation(updatedAllocations, 0, 0);
        });

        it("when given several allocations and no floatTotal allocations will all be zero", () => {
            const updatedAllocations = updateFloatingValues(0, [ { working: { percentage: 30 }}, { working: { percentage: 30 }}]);
            assertAllocation(updatedAllocations, 0, 0);
            assertAllocation(updatedAllocations, 1, 0);
        });

        it("float should be equal to 50 if floating total equals 100 and 2 floats", () => {
             const fl1 = { working: { type: "FLOATING" }};
             const fl2 = { working: { type: "FLOATING" }};
             const updatedAllocations = updateFloatingValues(100, [fl1, fl2]);
             assertAllocation(updatedAllocations, 0, 50);
             assertAllocation(updatedAllocations, 1, 50);
         });

        it("cannot update a null array of floating values", () => {
            assert.throws(() => updateFloatingValues(0, null), /null/);
        });

        it("floating totals are clamped between 0 and 100 (low)", () => {
            const updatedAllocations = updateFloatingValues(-10, [{ working: {}}]);
            assertAllocation(updatedAllocations, 0, 0);
        });

        it("floating totals are clamped between 0 and 100 (high)", () => {
            const updatedAllocations = updateFloatingValues(110, [{ working: {}}]);
            assertAllocation(updatedAllocations, 0, 100);
        });

        it("decimals are okay", () => {
            const updatedAllocations = updateFloatingValues(3, [{ working: {}}, { working: {}}]);
            assertAllocation(updatedAllocations, 0, 1.5);
            assertAllocation(updatedAllocations, 1, 1.5);
        });

    });


});
