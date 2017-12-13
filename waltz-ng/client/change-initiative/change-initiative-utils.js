/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import * as _ from "lodash";

export function groupRelationships(ciId, rels = []) {
    return _.chain(rels)
        .flatMap(rel => ([
            {entity: rel.a, relationship: rel.relationship},
            {entity: rel.b, relationship: rel.relationship}
        ]))
        .reject(rel => rel.entity.kind === 'CHANGE_INITIATIVE' && rel.entity.id === ciId)
        .groupBy('entity.kind', 'entity.id')
        .value();
}


export function enrichRelationships(rels = [], entities = []) {
    const entitiesById = _.keyBy(entities, 'id');

    return _.chain(rels)
        .map(rel => Object.assign({}, {
            relationship: rel.relationship,
            entity: entitiesById[rel.entity.id]
        }))
        .filter(rel => rel.entity)
        .value();
}