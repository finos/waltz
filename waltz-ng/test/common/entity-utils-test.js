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
import * as eu from "../../client/common/entity-utils";

const ref1app = {
    id: 1,
    kind: "APPLICATION"
};

const ref1ou = {
    id: 1,
    kind: "ORG_UNIT"
};

const ref2app = {
    id: 2,
    kind: "APPLICATION"
};

const notARef = {
    id: 2,
    wibble: "ORG_UNIT"
};


describe("common/entity-utils", () => {
    describe("sameRef", () => {

        it("matches if references are same", () => {
            assert.isTrue(eu.sameRef(ref1app, ref1app));
        });

        it("matches if references are equivalent", () => {
            assert.isTrue(eu.sameRef(ref1app, Object.assign({}, ref1app)));
        });

        it("matches if references are the same in essence", () => {
            assert.isTrue(eu.sameRef(ref1app, Object.assign({}, ref1app, { name: "Superfluous name"})));
        });

        it("fails if refs are not the same", () => {
            assert.isFalse(eu.sameRef(ref1app, ref2app));
            assert.isFalse(eu.sameRef(ref1app, ref1ou));
        });

        it("throws if either ref is missing", () => {
            assert.throws(() => eu.sameRef(ref1app, null));
            assert.throws(() => eu.sameRef(null, ref2app));
        });

        it("throws if either ref is not a ref", () => {
            assert.throws(() => eu.sameRef(ref1app, notARef));
            assert.throws(() => eu.sameRef(notARef, ref2app));
        });
    });

    describe("refToString", () => {
        it("fails if not a ref", () => assert.throws(() => eu.refToString(notARef)));

        it("renders if as KIND/ID if given a valid ref", () =>
            assert.equal(
                "APPLICATION/1",
                eu.refToString(ref1app)));

        it("ignores name and description", () =>
            assert.equal(
                "APPLICATION/1",
                eu.refToString(Object.assign({}, ref1app, {name: "n", description: "d"}))));
    });

    describe("toEntityRef", () => {
        it("makes an entity reference from a given object with an id and a given kind", () => {
            assert.equal(
                "APPLICATION/12",
                eu.refToString(eu.toEntityRefWithKind({id: 12}, "APPLICATION")));
        });

        it("gives back a reference if the given object has kind and id", () => {
            assert.equal(
                "ORG_UNIT/1",
                eu.refToString(eu.toEntityRef(ref1ou)));
        });

        it("preserves name and description if possible", () => {
            const r = eu.toEntityRef(Object.assign({} , ref1app, { name: "n", description: "d"}));
            assert.equal("n", r.name);
            assert.equal("d", r.description);
        });
    });
});

