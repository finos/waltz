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

import {assert} from "chai";
import Service from "../../../client/common/services/BaseLookupService";


describe("BaseLookupService: general", () => {

    it("throws error if registering a non-string type", () => {
        const service = new Service();
        assert.throws(() => service.register({}, {}));
    });


    it("throws error if registering a without a map of initial values", () => {
        const service = new Service();
        assert.throws(() => service.register("type", "not-a-map"));
    });


    it("returns ??'s around key if type is unknown", () => {
        const service = new Service();
        const val = service.lookup("unk", "key");
        assert.equal("??key??", val);
    });


    it("returns empty string for unknown keys", () => {
        const service = new Service();
        service.register("t", {});
        assert.equal("", service.lookup("t", "key"));
    });


    it("lookup values based on type and name", () => {
        const service = new Service();
        service.register("t", {a: "Aye"});
        assert.equal(service.lookup("t", "a"), "Aye");
    });


    it("subsequent registrations to same type will merge maps", () => {
        const service = new Service();

        service.register("t", {a: "anaconda", b: "bee"});
        assert.equal(service.lookup("t", "a"), "anaconda");
        assert.equal(service.lookup("t", "b"), "bee");

        service.register("t", {a: "aardvark", c: "cat"});
        assert.equal(service.lookup("t", "a"), "aardvark");
        assert.equal(service.lookup("t", "b"), "bee");
        assert.equal(service.lookup("t", "c"), "cat");

    });
});
