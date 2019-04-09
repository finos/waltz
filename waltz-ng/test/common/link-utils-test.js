import {kindToViewState, viewStateToKind} from "../../client/common/link-utils";
import {assert} from "chai";

describe("LinkUtils", () => {
    describe("kindToViewState", () => {
        it("can resolve known entity kinds", () => {
            assert.equal(kindToViewState("APPLICATION"), "main.app.view");
        });
        it("can throws exception if an unknown kind is given", () => {
            assert.throws(() => kindToViewState("WIBBLE"));
        });
    });

    describe("viewStateToKind", () => {
        it("can resolve known states", () => {
            assert.equal(viewStateToKind("main.app.view"), "APPLICATION");
        });
        it("can throws exception if an unknown state is given", () => {
            assert.throws(() => kindToViewState("main.wibble.wobble"));
        });
    });
});
