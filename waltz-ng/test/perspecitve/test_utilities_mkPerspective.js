import assert from "assert";
import {perspectiveDefinitions, measurables} from '../domain-objects/index';

import {mkPerspective} from "../../client/perspective/perpective-utilities";

describe('mkPerspective', () => {

    const pd = perspectiveDefinitions.PROCESS_REGION;

    it('needs a definition', () => {
        assert.throws(() => mkPerspective(null, []));
    });

    it('must look like a definition', () => {
        assert.throws(() => mkPerspective({ silly: 1 }, []))
    });

    it('must be given an array of measurable ratings', () => {
        assert.throws(() => mkPerspective(pd, [], "not an array"))
    });

    it('all good with little data', () => {
        const p = mkPerspective(pd, [], [], {});
        assert.deepEqual(p.definition, pd);
        assert.deepEqual(p.axes.x, []);
        assert.deepEqual(p.axes.y, []);
    });

});


