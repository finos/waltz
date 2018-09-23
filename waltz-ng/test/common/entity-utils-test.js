import assert from "assert";
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


describe("sameRef", () => {

    it("matches if references are same", () => {
        assert(eu.sameRef(ref1app, ref1app));
    });

    it("matches if references are equivalent", () => {
        assert(eu.sameRef(ref1app, Object.assign({}, ref1app)));
    });

    it("matches if references are the same in essence", () => {
        assert(eu.sameRef(ref1app, Object.assign({}, ref1app, { name: "Superfluous name"})));
    });

    it("fails if refs are not the same", () => {
        assert(!eu.sameRef(ref1app, ref2app));
        assert(!eu.sameRef(ref1app, ref1ou));
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
            eu.refToString(eu.toEntityRef({id: 12}, "APPLICATION")));
    });

    it("gives back a reference if the given object has kind and id", () => {
        assert.equal(
            "ORG_UNIT/1",
            eu.refToString(eu.toEntityRef(ref1ou)));
    });

    it("throws if the given object has no 'id'", () => {
        assert.throws(() => {
            eu.toEntityRef({name: "bob"});
        })
    });

    it("preserves name and description if possible", () => {
        const r = eu.toEntityRef(Object.assign({} , ref1app, { name: "n", description: "d"}));
        assert.equal("n", r.name);
        assert.equal("d", r.description);
    });
});