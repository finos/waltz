import _ from "lodash";
import {ensureIsArray, ensureNotNull, ensureIsNumber} from "../../common/checks";

export default class {

    getImmediateHierarchy(allOrgUnits, idThing) {

        ensureIsArray(allOrgUnits, 'allOrgUnits not an array');
        const id = ensureIsNumber(idThing, 'id is not a number');

        const unit = _.find(allOrgUnits, { id: Number(id) });

        ensureNotNull(unit, `Could not find org-unit for id ${id}`);
        const parent = _.find(allOrgUnits, { id: Number(unit.parentId) });
        const children = _.filter(allOrgUnits, { parentId: Number(id) });

        return {
            parent,
            unit,
            children
        };
    }
}
