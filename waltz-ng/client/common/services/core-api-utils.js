/*
 *
 *  * Waltz - Enterprise Architecture
 *  * Copyright (C) 2017  Khartec Ltd.
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Lesser General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import {EntityNamedNoteStore_API as EntityNamedNoteStore} from '../../entity-named-note/services/entity-named-note-store';
import {EntityNamedNoteTypeStore_API as EntityNamedNoteTypeStore} from '../../entity-named-note/services/entity-named-note-type-store';
import {MeasurableStore_API as MeasurableStore} from '../../measurable/services/measurable-store';
import {MeasurableCategoryStore_API as MeasurableCategoryStore} from '../../measurable-category/services/measurable-category-store';
import {MeasurableRelationshipStore_API as MeasurableRelationshipStore} from '../../measurable-relationship/services/measurable-relationship-store';


export const CORE_API = {
    EntityNamedNoteStore,
    EntityNamedNoteTypeStore,
    MeasurableStore,
    MeasurableCategoryStore,
    MeasurableRelationshipStore,
};


export function getApiReference(serviceName, serviceFnName) {
    return CORE_API[serviceName][serviceFnName];
}


