/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */

import assert from "assert";
import { randomPick } from "../../client/common";


describe("randomPick", () => {

    it("returns the only element of a one element array", () => {
        assert.equal(3, randomPick([3]));
    });

    it("returns null from a zero element array", () => {
        assert.equal(null, randomPick([]));
    });

    it("throws with a null array", () => {
        assert.throws(() => randomPick());
    });

});
