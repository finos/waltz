import assert from "assert";
import Service from "../../../client/common/services/BaseLookupService";


describe('base lookup service', () => {

    it('throws error if registering a non-string type', () => {
        const service = new Service();
        assert.throws(() => service.register({}, {}));
    });


    it('throws error if registering a without a map of initial values', () => {
        const service = new Service();
        assert.throws(() => service.register('type', 'not-a-map'));
    });


    it('returns ??\'s around key if type is unknown', () => {
        const service = new Service();
        const val = service.lookup('unk', 'key');
        assert.equal('??key??', val);
    });


    it('returns empty string for unknown keys', () => {
        const service = new Service();
        service.register('t', {});
        assert.equal('', service.lookup('t', 'key'));
    });


    it('lookup values based on type and name', () => {
        const service = new Service();
        service.register('t', {a: 'Aye'});
        assert.equal(service.lookup('t', 'a'), 'Aye');
    });


    it('subsequent registrations to same type will merge maps', () => {
        const service = new Service();

        service.register('t', {a: 'anaconda', b: 'bee'});
        assert.equal(service.lookup('t', 'a'), 'anaconda');
        assert.equal(service.lookup('t', 'b'), 'bee');

        service.register('t', {a: 'aardvark', c: 'cat'});
        assert.equal(service.lookup('t', 'a'), 'aardvark');
        assert.equal(service.lookup('t', 'b'), 'bee');
        assert.equal(service.lookup('t', 'c'), 'cat');

    });
});
