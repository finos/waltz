
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

import { shallowDiff } from '../../client/common';


describe('shallowDiff', () => {

    it('should give nothing when comparing empties', () => {
        assert.equal(0, shallowDiff({}, {}).length);
    });

    it('should give nothing when comparing against null', () => {
        assert.equal(0, shallowDiff(null, {}).length);
    });

    it('should give nothing when comparing against null', () => {
        assert.equal(0, shallowDiff({}, null).length);
    });

    it('should report new prop ', () => {
        const diff = shallowDiff({ a: 1 }, {});
        assert.equal('a', diff[0].field);
        assert.equal(1, diff[0].newValue);
    });


    it('should report removed prop ', () => {
        const diff = shallowDiff({ a: 1 }, { a: 1, b: 2 });
        assert.equal(1, diff.length);
        assert.equal('b', diff[0].field);
        assert.equal(2, diff[0].oldValue);
        assert.equal(undefined, diff[0].newValue);
    });

    it('should report both removed and added props', () => {
        const diff = shallowDiff({ a: 1, b: 2, x: 3}, { a: 1, x: 3, y: 4 } );
        assert.equal(2, diff.length);
        assert.deepEqual(['b', 'y'], _.map(diff, 'field'));
    });

});
