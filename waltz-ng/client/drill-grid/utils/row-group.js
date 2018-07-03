
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

import _ from 'lodash';


/**
 * Given three lists of app ids representing direct mappings, heirs and ancestors
 * this function returns a value describing the type of mapping that describes
 * the given applications association:
 *
 * - DIRECT
 * - HEIR
 * - ANCESTOR
 * - NONE (app has other mappings)
 * - UNKNOWN (app has no mappings at all)
 *
 *
 * @param app ({ id, hasMappings } )
 * @param directs
 * @param heirs
 * @param ancestors
 * @returns {*}
 */
function determineMappingType(app, directs = [], heirs = [], ancestors = []) {
    if (_.includes(directs, app.id)) { return 'DIRECT'; }
    else if (_.includes(heirs, app.id)) { return 'HEIR'; }
    else if (_.includes(ancestors, app.id)) { return 'ANCESTOR'; }
    else { return app.hasMappings ? 'NONE' : 'UNKNOWN'; }
}


/**
 * determines a rating (RAGZ) for a given set of mappings.
 *
 * @param mappings { direct, heirs, inherited }
 * @param appId
 * @returns {string}
 */
function determineRating(mappings, appId) {
    const findRatings = rs => _
        .chain(rs)
        .filter(r => r.app.id === appId)
        .map('rating')
        .value();

    return _.head(findRatings(mappings.direct))
        || _.head(findRatings(mappings.heirs))
        || _.head(findRatings(mappings.inherited))
        || 'Z';
}


/**
 * Given a collection of mappings, extracts a unique list of application references
 * @param mappings
 */
function getAppsFromMappings(mappings, focusApp) {
    return _
        .chain(mappings)
        .values()
        .flatten()
        .map(m => m.app)
        .uniqBy(a => a.id)
        .filter(a => focusApp ? a.id === focusApp.id : true)
        .value();
}



// -- CLASS ---

export default class RowGroup {

    constructor(yDatum, xAxis, focusApp) {

        const xDomain = xAxis.current.domain || [];
        if (xDomain.length == 0){
            return null;
        }
        const rowApplications = getAppsFromMappings(yDatum.mappings, focusApp);
        const colApplications = _.flatMap(
            xDomain,
            xDatum => getAppsFromMappings(xDatum.mappings, focusApp));

        const appsWithBothDimensions = _.intersectionBy(rowApplications, colApplications, 'id'); //_.unionBy(rowApplications, colApplications, 'id');
        const appsWithOnlyRowDimension = _.differenceBy(rowApplications, appsWithBothDimensions, 'id');

        const directAppIds = _.map(yDatum.mappings.direct, 'app.id');
        const ancestorAppIds = _.map(yDatum.mappings.inherited, 'app.id');
        const heirAppIds = _.map(yDatum.mappings.heirs, 'app.id');

        const rows = _
            .chain(appsWithBothDimensions)
            .sortBy('name')
            .map(app => {
                const rowType = determineMappingType(app, directAppIds, heirAppIds, ancestorAppIds);
                if (rowType === 'NONE') {
                    console.log('should never happen')
                    return null;
                }

                const mappings = _.map(xDomain, xDatum => {
                    const colType = determineMappingType(
                        app,
                        _.map(xDatum.mappings.direct, 'app.id'),
                        _.map(xDatum.mappings.heirs, 'app.id'),
                        _.map(xDatum.mappings.inherited, 'app.id'));

                    const rating = determineRating(xDatum.mappings, app.id);

                    return {
                        colId: xDatum.id,
                        groupId: yDatum.id,
                        colType,
                        col: xDatum,
                        row: yDatum,
                        app: app,
                        rating
                    };
                });
                return {
                    app,
                    rowType,
                    mappings
                };
            })
            .compact()
            .reject(row => _.every(row.mappings, m => m.colType === 'NONE'))
            .compact()
            .value();

        this.group = yDatum;
        this.rows = rows;
        this.rowOnlyApplications = appsWithOnlyRowDimension;
    }


    isEmpty() {
        return _.isEmpty(this.rows)
    }
}
