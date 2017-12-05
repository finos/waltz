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

import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {AUTH_SOURCE_NAVIGATOR_CATEGORY_ID} from "../../../system/services/settings-names";

import template from './auth-sources-navigator-panel.html';
import HierarchicalAxis from "../../utils/hierarchical-axis";
import DrillGrid from "../../utils/drill-grid";


const bindings = {
    parentEntityRef: '<',
};


const initialState = {
    xAxis: {
        kind: 'MEASURABLE_CATEGORY',
        id: 7
    },
    yAxis: {
        kind: 'MEASURABLE_CATEGORY',
        id: 1
    },
    visibility: {
        chart: false
    }
};

function prepareMappings(rawMappings, domain, axisDefinition, relevantAppsById) {
    if (axisDefinition.kind === 'DATA_TYPE') {
        const dataTypesByCode = _.keyBy(domain, 'code');
        return normalizeMappings(
            rawMappings,
            m => dataTypesByCode[m.dataType].id,
            m => relevantAppsById[m.applicationReference.id],
            m => 'Z');
    } else {
        return prepareMeasurableMappings(
            rawMappings,
            relevantAppsById);
    }
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


function prepareMeasurableMappings(ratings, appsById) {
    // only interested in ratings for the auth sources
    const authRatings = _.filter(ratings, m => appsById[m.entityReference.id]);

    return normalizeMappings(
        authRatings,
        m => m.measurableId,
        m => appsById[m.entityReference.id],
        m => m.rating);
}


function determineAppsById(colMappings, rowMappings, xAxis, yAxis, allApps) {
    const limitToAuthSources = (mappings) => _
        .chain(mappings)
        .map('applicationReference')
        .keyBy('id')
        .value();

    if (xAxis.kind === 'DATA_TYPE'){
        return limitToAuthSources(colMappings);
    } else if (yAxis.kind === 'DATA_TYPE') {
        return limitToAuthSources(rowMappings);
    } else {
        return _.keyBy(allApps, 'id');
    }
}


function controller($element, $timeout, $q, serviceBroker, settingsService) {
    const vm = initialiseData(this, initialState);

    const loadChartData = () => {

        const mkDomainPromise = (axis) => axis.kind === 'DATA_TYPE'
            ? serviceBroker.loadAppData(CORE_API.DataTypeStore.findAll).then(r => r.data)
            : serviceBroker.loadAppData(CORE_API.MeasurableStore.findAll).then(r => _.filter(r.data, { categoryId: axis.id }));

        const mkMappingPromise = (axis) => axis.kind === 'DATA_TYPE'
            ? serviceBroker.loadViewData(CORE_API.AuthSourcesStore.findAuthSources, [ vm.parentEntityRef ]).then(r => r.data)
            : serviceBroker.loadViewData(CORE_API.MeasurableRatingStore.findByCategory, [ axis.id ]).then(r => r.data);

        const promises = [
            mkDomainPromise(vm.xAxis),
            mkDomainPromise(vm.yAxis),
            mkMappingPromise(vm.xAxis),
            mkMappingPromise(vm.yAxis)
        ];

        $q.all(promises)
            .then(([colDomain, rowDomain, rawColMappings, rawRowMappings]) => {
                console.log('load', { colDomain, rowDomain, rawColMappings, rawRowMappings });

                const messages = [];
                let canShow = true;

                if (_.isEmpty(rawColMappings)) {
                    canShow = false;
                    messages.push(`No data for x-axis: ${vm.xAxis.name}`);
                }

                if (_.isEmpty(rawRowMappings)) {
                    canShow = false;
                    messages.push(`No data for y-axis: ${vm.yAxis.name}`);
                }

                vm.messages = messages;
                vm.visibility.chart = canShow;

                if (! canShow) {
                    return;
                }

                const appsById = determineAppsById(rawColMappings, rawRowMappings, vm.xAxis, vm.yAxis, vm.allApps);
                const colMappings = prepareMappings(rawColMappings, colDomain, vm.xAxis, appsById);
                const rowMappings = prepareMappings(rawRowMappings, rowDomain, vm.yAxis, appsById);

                const colData = {
                    domain: colDomain,
                    mappings: colMappings
                };

                const rowData = {
                    domain: rowDomain,
                    mappings: rowMappings
                };

                const drillGrid = new DrillGrid(
                    new HierarchicalAxis(colData),
                    new HierarchicalAxis(rowData));

                drillGrid.refresh();
                vm.drillGrid = drillGrid;
            });
    };

    vm.$onInit = () => {
        const promises = [
            serviceBroker
                .loadViewData(CORE_API.ApplicationStore.findBySelector, [ mkSelectionOptions(vm.parentEntityRef) ])
                .then(r => r.data),
            serviceBroker
                .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
                .then(r => r.data),
            settingsService
                .findOrDie(AUTH_SOURCE_NAVIGATOR_CATEGORY_ID, 'Cannot find auth source navigator measurable category')
        ];

        $q.all(promises)
            .then(([apps, categories, defaultCategoryId]) => {
                console.log('asnp, disabled default category id ', { defaultCategoryId })
                vm.allApps = apps;
                console.log('allApps', { all: vm.allApps });
                vm.categories = categories;
                vm.categoriesById = _.keyBy(categories, 'id');

                vm.axisOptions = _.union(
                    categories,
                    [ { id: null, kind: 'DATA_TYPE', name: 'Data Type' }]);

                loadChartData();
            });
    };

    vm.onAxisChange = () => {
        loadChartData();
    };
}


controller.$inject = [
    '$element',
    '$timeout',
    '$q',
    'ServiceBroker',
    'SettingsService'
];


const component = {
    controller,
    bindings,
    template
};


export default {
    id: 'waltzAuthSourcesNavigatorPanel',
    component
}