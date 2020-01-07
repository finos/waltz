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