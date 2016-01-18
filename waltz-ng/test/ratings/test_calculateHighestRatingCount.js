
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

import assert from 'assert';
import _ from 'lodash';

import { calculateHighestRatingCount } from '../../client/ratings/directives/common';


describe('calculateHighestRatingCount', () => {

    it('should give zero if given an empty list of groups', () => {
        const result = calculateHighestRatingCount([]);
        assert.equal(0, result);
    });

    it('should give zero if given a null list of groups', () => {
        const result = calculateHighestRatingCount();
        assert.equal(0, result);
    });

    it('simple case of only one group with one summary', () => {
        const result = calculateHighestRatingCount([{ summaries: { a: 1 } }]);
        assert.equal(1, result);
    });

    it('finds highest in multiple one element groups', () => {
        const result = calculateHighestRatingCount([
            { summaries: { a: 1 } },
            { summaries: { b: 3 } },
            { summaries: { c: 2 } }
        ]);
        assert.equal(3, result);
    });

    it('finds highest in multiple many element groups', () => {
        const result = calculateHighestRatingCount([
            { summaries: { a: 1, aa: 10 } },
            { summaries: { b: 3, bb: 33 } },
            { summaries: { c: 2, cc: 22 } }
        ]);
        assert.equal(33, result);
    });


});
