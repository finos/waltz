
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



function scoreMapping(app, directs = [], heirs = [], ancestors = []) {
    if (_.includes(directs, app.id)) { return 'DIRECT'; }
    else if (_.includes(heirs, app.id)) { return 'HEIR'; }
    else if (_.includes(ancestors, app.id)) { return 'ANCESTOR'; }
    else { return app.hasMappings ? 'NONE' : 'UNKNOWN'; }
}


function determineRating(colMappings, appId) {
    const findRatings = rs => _
        .chain(rs)
        .filter(r => r.app.id === appId)
        .map('rating')
        .value();

    const directRatings = findRatings(colMappings.direct, appId);
    const heirRatings = findRatings(colMappings.heirs, appId);
    const ancestorRatings = findRatings(colMappings.inherited, appId);

    const ratings = _.concat(directRatings, heirRatings, ancestorRatings);
    return _.head(ratings) || 'Z';
}



// -- CLASS ---

export default class RowGroup {

    constructor(yDatum, xAxis) {
        const getApps = mappings => _
            .chain(mappings)
            .values()
            .flatten()
            .map(m => m.app)
            .value();

        const rowApplications = getApps(yDatum.mappings);

        const colsDomain = xAxis.current.domain || [];
        if (colsDomain.length == 0){
            return null;
        }

        const colApplications = _
            .chain(colsDomain)
            .flatMap(d => getApps(d.mappings))
            .uniqBy('id')
            .value();

        const applications = _.unionBy(rowApplications, colApplications, 'id');

        const directAppIds = _.map(yDatum.mappings.direct, 'app.id');
        const ancestorAppIds = _.map(yDatum.mappings.inherited, 'app.id');
        const heirAppIds = _.map(yDatum.mappings.heirs, 'app.id');

        const applicationsWithMappings = _
            .chain(applications)
            .sortBy('name')
            .map(app => {
                const rowType = scoreMapping(app, directAppIds, heirAppIds, ancestorAppIds);
                if (rowType === 'NONE') {
                    return null;
                }

                const mappings = _.map(colsDomain, xDatum => {
                    const colType = scoreMapping(
                        app,
                        _.map(xDatum.mappings.direct, 'app.id'),
                        _.map(xDatum.mappings.heirs, 'app.id'),
                        _.map(xDatum.mappings.inherited, 'app.id'));

                    const rating = determineRating(xDatum.mappings, app.id);

                    return {
                        colId: xDatum.id,
                        groupId: yDatum.id,
                        rowType,
                        colType,
                        rating
                    };
                });
                return {
                    app,
                    mappings
                };
            })
            .compact()
            .reject(row => _.every(row.mappings, m => m.colType === 'NONE'))
            .compact()
            .value();

        return {
            domain: yDatum,
            rows: applicationsWithMappings
        };
    }

}
