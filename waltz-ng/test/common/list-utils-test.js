import assert from 'assert';
import * as lu from '../../client/common/list-utils';

const abc = ['a', 'b', 'c'];
const abcd = ['a', 'b', 'c', 'd'];


describe('containsAll', () => {
    it ("returns true if all items in the second list are contained in the first",
        () => assert(lu.containsAll(abc, abc)));

    it ("true if the first is a superset",
        () => assert(lu.containsAll(abcd, abc)));

    it ("true if the second is empty",
        () => assert(lu.containsAll(abcd, [])));

    it ("true if the second is null",
        () => assert(lu.containsAll(abcd, null)));

    it ("true if both are empty",
        () => assert(lu.containsAll([], [])));

    it ("true if both are null",
        () => assert(lu.containsAll(null, null)));

    it ("false if second contains things first doesn't have",
        () => assert(! lu.containsAll(abc, abcd)));
});





