
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
import {calculateGroupSummary} from "../../client/ratings/directives/common";


describe('calculateGroupSummary', () => {

    it('should calculate a summary by transposing and counting', () => {
        const result = calculateGroupSummary([
            { ratings: [ {current: 'A'}, {current: 'B'}, {current: 'C'}] },
            { ratings: [ {current: 'A'}, {current: 'A'}, {current: 'A'}] }
        ]);

        assert.deepEqual(result, [{ A: 2 }, { B: 1, A: 1}, { C: 1, A: 1 } ]);
    });

    it('should give empty array for empty input array', () => {
        const result = calculateGroupSummary([]);
        assert.deepEqual([], result);
    });

    it('should give empty array for null input', () => {
        const result = calculateGroupSummary();
        assert.deepEqual([], result);
    });

});
