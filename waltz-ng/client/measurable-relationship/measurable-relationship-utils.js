

/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import _ from 'lodash';


export function determineCounterpart(measurableId, rel) {
    return rel.a.kind === 'MEASURABLE' && rel.a.id === measurableId
        ? rel.b
        : rel.a;
}


export function sanitizeRelationships(relationships, measurables, categories) {
    const categoriesById = _.keyBy(categories, c => c.id);
    const measurablesById = _.keyBy(measurables, m => m.id);

    const isValid = (mId) => measurablesById[mId] && categoriesById[measurablesById[mId].categoryId];
    const isValidRel = (rel) => {
        const aValid = rel.a.kind === 'MEASURABLE'
            ? isValid(rel.a.id)
            : true;
        const bValid = rel.b.kind === 'MEASURABLE'
            ? isValid(rel.b.id)
            : true;

        return aValid && bValid;
    };

    return _.filter(relationships, isValidRel);
}
