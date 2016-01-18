import assert from 'assert';

import Service from '../../../client/org-units/services/org-unit-utility';


const service = new Service();


const p = { id: 1 };
const n = { id: 2, parentId: 1 };
const c1 = { id: 3, parentId: 2 };
const c2 = { id: 4, parentId: 2 };


describe('getImmediateHierarchy', () => {

    it('should give parent, children and the focused unit', () => {
        const result = service.getImmediateHierarchy([p, n, c1, c2], n.id);
        assert.equal(n, result.unit);
        assert.equal(p, result.parent);

        const kids = result.children;
        assert.equal(2, kids.length);

        assert(kids.indexOf(c1) >= -1);
        assert(kids.indexOf(c2) >= -1);
    });


    it('should throw if the first argument is not defined', () => {
        assert.throws(() => service.getImmediateHierarchy(), Error);
    });


    it('should throw if the first argument is not array', () => {
        assert.throws(() => service.getImmediateHierarchy({}), Error);
    });


    it('should throw if the second argument is not a number', () => {
        assert.throws(() => service.getImmediateHierarchy([], 'hello'), Error);
    });


    it('if no parent, then parent is set to null', () => {
        const result = service.getImmediateHierarchy([n, c1, c2], n.id);
        assert.equal(null, result.parent);
    });


    it('if no children, then children is set to []', () => {
        const result = service.getImmediateHierarchy([n], n.id);
        assert.equal(0, result.children.length);
    });


    it('if no matching unit, then Error is thown', () => {
        assert.throws(() => service.getImmediateHierarchy([], n.id));
    });

});
