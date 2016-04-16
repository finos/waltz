/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */

import d3 from "d3";
import _ from "lodash";


const byAppThenTypeThenRatingNester = d3.nest()
    .key(f => f.targetApp.id)
    .key(f => f.dataFlow.dataType)
    .key(f => f.rating);


const summariseChildDataFn = d3.nest()
    .key(f => f.dataFlow.dataType)
    .key(f => f.rating)
    .rollup(flows => flows.length);


function calculateInvolvedDataTypes(ratedDataFlows) {
    return _.chain(ratedDataFlows)
        .map('dataFlow.dataType')
        .uniq()
        .value();
}


function decorateFlowsWithTargetApp(ratedDataFlows, appsById) {
    return _.chain(ratedDataFlows)
        .map(f => {
            const targetApp = appsById[f.dataFlow.target.id];
            return {
                ...f,
                targetApp
            };
        })
        .value();
}

function calculateFlatOrgTree(orgUnitId, orgUnitsByParent) {
    const allChildIds = (id) => {
        const children = orgUnitsByParent[id];
        return _.union([id], children ? _.map(children, c => allChildIds(c.id)) : []);
    };

    return _.map(orgUnitsByParent[orgUnitId], p => ({ id: p.id, childrenIds: _.flatten(allChildIds(p.id), true)}));
}


function decorateAndIndexApps(applications, orgUnitsById) {
    return _.chain(applications)
        .map(a => ({...a, orgUnit: orgUnitsById[a.organisationalUnitId]}))
        .keyBy('id')
        .value();
}


export default class {

    constructor(ratedDataFlows = [], applications = [], orgUnits = [], orgUnitId = 0) {
        this.prepareData(orgUnitId, orgUnits, applications, ratedDataFlows);
    }


    /**
     * takes ratedFlows of the form: { rating, dataFlow }
     * and gives back objects like: { rating, dataFlow, sourceApp, targetApp }
     * where sourceApp and targetApp have also been enriched to with organisational units
     */
    prepareData(orgUnitId, orgUnits, applications, ratedDataFlows) {
        this.orgUnitsById = _.keyBy(orgUnits, 'id');
        this.appsById = decorateAndIndexApps(applications, this.orgUnitsById);
        this.orgUnitsByParent = _.groupBy(orgUnits, ou => ou.parentId);
        this.dataTypes = calculateInvolvedDataTypes(ratedDataFlows);

        const enrichedRatedDataFlows = decorateFlowsWithTargetApp(ratedDataFlows, this.appsById);
        const selfFlows = _.filter(enrichedRatedDataFlows, f => f.targetApp.organisationalUnitId === orgUnitId);
        const flowsByAppThenTypeThenRating = _.map(
            byAppThenTypeThenRatingNester.entries(selfFlows),
            a => ({ ...a, app: this.appsById[a.key]})
        );

        const flatOrgTree = calculateFlatOrgTree(orgUnitId, this.orgUnitsByParent);

        const childData = _.map(flatOrgTree, child => {
            const isChildFlowFn = f => _.includes(child.childrenIds, f.targetApp.organisationalUnitId);
            const relevantFlows = _.filter(enrichedRatedDataFlows, isChildFlowFn);
            return {
                id: child.id,
                orgUnit: this.orgUnitsById[child.id],
                stats: summariseChildDataFn.entries(relevantFlows)
            };
        });

        this.root = {
            id: orgUnitId,
            orgUnit: this.orgUnitsById[orgUnitId],
            dataTypes: this.dataTypes,
            flowsByAppThenTypeThenRating,
            children: childData
        };
    }

}