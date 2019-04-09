import { assert } from "chai";
import {mkSelectionOptions} from "../../client/common/selector-utils";
import {mkRef, sameRef} from "../../client/common/entity-utils";

const appRef = mkRef("APPLICATION", 1);
const orgRef = mkRef("ORG_UNIT", 2);

describe("SelectorUtils", () => {
    describe("mkSelectionOptions", () => {

        it("can make selection options from a simple entity appRef", () => {
            const opts = mkSelectionOptions(appRef);
            assert.isNotNull(opts);
            assert.isTrue(sameRef(opts.entityReference, appRef));
            assert.equal(opts.scope, "EXACT");
        });

        it("can infer scope for an orgRef", () => {
            const opts = mkSelectionOptions(orgRef);
            assert.equal(opts.scope, "CHILDREN");
        });

        it("can allow override of scope for an orgRef", () => {
            const opts = mkSelectionOptions(orgRef, "EXACT");
            assert.equal(opts.scope, "EXACT");
        });

        it("defaults lifecycle statuses to ['ACTIVE']", () => {
            const opts = mkSelectionOptions(appRef);
            assert.deepEqual(["ACTIVE"], opts.entityLifecycleStatuses);
        });

        it("allows override of lifecycle statuses", () => {
            const opts = mkSelectionOptions(appRef, "EXACT", ["ACTIVE", "RETIRED"]);
            assert.deepEqual(["ACTIVE", "RETIRED"], opts.entityLifecycleStatuses);
        });
    });
});