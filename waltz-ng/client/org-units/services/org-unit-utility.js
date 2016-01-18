import _ from 'lodash';

import { ensureIsArray, ensureNotNull, ensureIsNumber } from '../../common/checks';

export default class {

    getImmediateHierarchy(allOrgUnits, idThing) {

        ensureIsArray(allOrgUnits, 'allOrgUnits not an array');
        const id = ensureIsNumber(idThing, 'id is not a number');

        const unit = _.findWhere(allOrgUnits, { id: Number(id) });

        ensureNotNull(unit, `Could not find org-unit for id ${id}`);
        const parent = _.findWhere(allOrgUnits, { id: Number(unit.parentId) });
        const children = _.where(allOrgUnits, { parentId: Number(id) });

        return {
            parent,
            unit,
            children
        };
    }
}
