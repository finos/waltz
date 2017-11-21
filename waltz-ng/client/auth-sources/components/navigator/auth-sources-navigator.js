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

import template from './auth-sources-navigator.html';
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {buildHierarchies, getParents, groupHierarchyByKey, indexHierarchyByKey} from "../../../common/hierarchy-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {toDomain} from "../../../common/string-utils";

const bindings = {
    parentEntityRef: '<',
    measurableCategoryId: '<'
};


const initialState = {
    chart: {
        cols: {
            parents: [],
            active: null,
            domain: [],
            colStyle: {}
        },
        rows: {
            parents: [],
            active: null,
            domain: []
        }
    }
};


function findAncestors(dataTypesById, dt) {
    const acc = [ ];
    let ptr = dt;
    while (ptr) {
        acc.push(ptr);
        ptr = dataTypesById[ptr.parentId];
    }
    return acc;
}


function recalcDomain(dataSet, parentId) {
    const potentialParents = _
        .chain(dataSet.nodesByParent[parentId] || [])
        .sortBy('name')
        .map()
        .value();

    return potentialParents;
}


function focus(dataSet, id) {
    const domain = recalcDomain(dataSet, id);
    if (domain.length == 0) {
        return;
    } else {
        return {
            domain,
            active: dataSet.nodesById[id],
            parents: getParents(dataSet.nodesById[id])
        };
    }
}


/**
 * Determine the domain entities required to build a minimal (but complete) hierarchy
 * encompassing the given mappings.  It does this by finding all ancestors of domain
 * items mentioned in the mappings.
 *
 * @param allDomain
 * @param mappings
 */
function determineRequiredDomainEntities(domainById = [], mappings = []) {
    return _
        .chain(mappings)
        .map(m => domainById[m.domainId])
        .compact()
        .uniqBy('id')
        .flatMap(m => findAncestors(domainById, m))
        .uniqBy('id')
        .value();
}



function mkHierarchyDataSet(requiredElems) {
    const hierarchy = buildHierarchies(requiredElems);
    const nodesById = indexHierarchyByKey(hierarchy, n => n.id);
    const nodesByParent = groupHierarchyByKey(hierarchy, n => n.parentId);

    return {
        hierarchy,
        nodesById,
        nodesByParent
    };
}


/**
 * Given a set of mappings converts them into a standard format using
 * the provided helper methods.
 *
 * The output is: { domainId, app, rating }
 *
 * @param mappings
 * @param domainIdProvider
 * @param appProvider
 * @param ratingProvider
 */
function normalizeMappings(mappings = [],
                          domainIdProvider,
                          appProvider,
                          ratingProvider) {
    return _
        .chain(mappings)
        .map(m => {
            return {
                domainId: domainIdProvider(m),
                app: appProvider(m),
                rating: ratingProvider(m)
            }
        })
        .value();
}


function populateRelated(hierarchyData) {
    const popParents = (node, mapping) => {
        _.each(
            getParents(node),
            parent => {
                parent.mappings.heirs = _.unionBy(
                    parent.mappings.heirs,
                    [ mapping ],
                    m => m.app.id)
            });
    };

    const popChildren = (node, mapping) => {
        _.each(
            node.children,
            child => {
                child.mappings.inherited = _.unionBy(
                    child.mappings.inherited,
                    [ mapping ],
                    m => m.app.id);
                popChildren(child, mapping);
            });
    };

    const byId = hierarchyData.nodesById;
    const nodes = _.values(byId);
    _.chain(nodes)
        .flatMap(n => n.mappings.direct)
        .each(mapping => {
            const node = byId[mapping.domainId];
            popParents(node, mapping);
            popChildren(node, mapping);
        })
        .value();
}


function prepareDataSet(allDomainEntities = [], mappings = []) {

    const mkInitMappings = () => ({
        mappings: {
            direct: [],
            inherited: [],
            heirs: []
        }
    });

    const domainEntities = _.map(
        allDomainEntities,
        d => Object.assign({}, d, mkInitMappings()));

    const domainById = _.keyBy(domainEntities, 'id');

    _.chain(mappings)
        .filter(m => domainById[m.domainId])
        .each(m => domainById[m.domainId].mappings.direct.push(m))
        .value();

    const requiredDomainEntities = determineRequiredDomainEntities(domainById, mappings);
    const hierarchyData = mkHierarchyDataSet(requiredDomainEntities);

    populateRelated(hierarchyData);

    return hierarchyData;
}


function controller($element, $q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = async () => {
        console.log('vm', vm)
        const selector = mkSelectionOptions(vm.parentEntityRef);

        const authSources = await serviceBroker
            .loadViewData(CORE_API.AuthSourcesStore.findAuthSources, [ vm.parentEntityRef ])
            .then(r => r.data);
        const dataTypes = await serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => r.data);
        const allMeasurables = await serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => _.filter(r.data, { categoryId: vm.measurableCategoryId }));
        const allRatings = await serviceBroker
            .loadViewData(CORE_API.MeasurableRatingStore.findByAppSelector, [ selector ])
            .then(r => r.data);

        vm.appsById = await serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findBySelector, [ selector ])
            .then(r => {
                const appsById = _.keyBy(r.data, 'id')
                const authSourceAppsById = _
                    .chain(authSources)
                    .map('applicationReference')
                    .map(a => Object.assign(a, { kind: 'APPLICATION' }))
                    .keyBy('id')
                    .value();

                return Object.assign({}, appsById, authSourceAppsById);
            });


        const dataTypesByCode = _.keyBy(dataTypes, 'code');
        const authSourceMappings = normalizeMappings(
                authSources,
                m => dataTypesByCode[m.dataType].id,
                m => vm.appsById[m.applicationReference.id],
                m => 'Z');
        const measurableMappings = normalizeMappings(
                allRatings,
                m => m.measurableId,
                m => vm.appsById[m.entityReference.id],
                m => m.rating);

        const dataTypeDataSet = prepareDataSet(dataTypes, authSourceMappings);
        const measurableDataSet = prepareDataSet(allMeasurables, measurableMappings);


        const focusRow = (id) => {
            const result = focus(dataTypeDataSet, id);
            if (result) {
                vm.chart.rows = result;
            }
        };

        const focusCol = (id) => {
            const result = focus(measurableDataSet, id);
            if (result) {
                vm.chart.cols = result;
                const numDomainValues = vm.chart.cols.domain.length || 1;
                vm.chart.cols.colStyle = {
                    width: `${70 / numDomainValues}%`
                };
            }
        };

        const focusBoth = (xId, yId) => {
            focusCol(xId);
            focusRow(yId);
        };


        const recalcRows = () => {
            const rowGroups = _
                .chain(vm.chart.rows.domain)
                .map(rowDomainValue => {
                    const getApps = mappings => _
                        .chain(mappings)
                        .values()
                        .flatten()
                        .map('app')
                        .value();

                    const rowApplications = getApps(rowDomainValue.mappings);
                    const colApplications = _
                        .chain(vm.chart.cols.domain)
                        .flatMap(d => getApps(d.mappings))
                        .uniqBy('id')
                        .tap(t => console.log('tap', t))
                        .value();

                    const applications = _.unionBy(rowApplications, colApplications, 'id');

                    const mappings = [];
                    return {
                        domain: rowDomainValue,
                        applications: _.sortBy(applications, 'name'),
                        mappings
                    };
                })
                .value();

            vm.chart.rows.groups = rowGroups;
        };

        vm.focusBoth = _.flow([focusBoth, recalcRows]);
        vm.focusCol = _.flow([focusCol, recalcRows]);
        vm.focusRow = _.flow([focusRow, recalcRows]);

        vm.focusBoth(null, null);

        global.chart = vm.chart;
    };

}


controller.$inject = ['$element', '$q', 'ServiceBroker'];


const component = {
    controller,
    bindings,
    template
};


export default {
    id: 'waltzAuthSourcesNavigator',
    component
}