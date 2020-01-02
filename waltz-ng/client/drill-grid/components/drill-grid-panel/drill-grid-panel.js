/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import _ from 'lodash';

import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {DRILL_GRID_DEFAULT_DEFINITION_ID} from "../../../system/services/settings-names";

import HierarchicalAxis from "../../utils/hierarchical-axis";
import DrillGrid from "../../utils/drill-grid";
import template from './drill-grid-panel.html';


const bindings = {
    parentEntityRef: '<',
};


const initialState = {
    visibility: {
        chart: false,
        loading: true,
        unavailable: false
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


function controller($q, serviceBroker, settingsService) {
    const vm = initialiseData(this, initialState);

    const loadChartData = (drillGridDefn) => {

        vm.visibility.loading = true;

        const mkDomainPromise = (axis) => axis.kind === 'DATA_TYPE'
            ? serviceBroker.loadAppData(CORE_API.DataTypeStore.findAll).then(r => r.data)
            : serviceBroker.loadAppData(CORE_API.MeasurableStore.findAll).then(r => _.filter(r.data, { categoryId: axis.id }));

        const mkMappingPromise = (axis) => axis.kind === 'DATA_TYPE'
            ? serviceBroker.loadViewData(CORE_API.AuthSourcesStore.findAuthSources, [ vm.parentEntityRef ]).then(r => r.data)
            : serviceBroker.loadViewData(CORE_API.MeasurableRatingStore.findByCategory, [ axis.id ]).then(r => r.data);

        const promises = [
            mkDomainPromise(drillGridDefn.xAxis),
            mkDomainPromise(drillGridDefn.yAxis),
            mkMappingPromise(drillGridDefn.xAxis),
            mkMappingPromise(drillGridDefn.yAxis)
        ];

        $q.all(promises)
            .then(() => vm.visibility.loading = false);

        $q.all(promises)
            .then(([colDomain, rowDomain, rawColMappings, rawRowMappings]) => {

                const messages = [];
                let canShow = true;

                if (_.isEmpty(rawColMappings)) {
                    canShow = false;
                    messages.push(`No data for x-axis: ${drillGridDefn.xAxis.name}`);
                }

                if (_.isEmpty(rawRowMappings)) {
                    canShow = false;
                    messages.push(`No data for y-axis: ${drillGridDefn.yAxis.name}`);
                }

                vm.messages = messages;
                vm.visibility.chart = canShow;

                if (! canShow) {
                    return;
                }

                const appsById = determineAppsById(rawColMappings, rawRowMappings, drillGridDefn.xAxis, drillGridDefn.yAxis, vm.allApps);
                const colMappings = prepareMappings(rawColMappings, colDomain, drillGridDefn.xAxis, appsById);
                const rowMappings = prepareMappings(rawRowMappings, rowDomain, drillGridDefn.yAxis, appsById);

                const colData = {
                    domain: colDomain,
                    mappings: colMappings
                };

                const rowData = {
                    domain: rowDomain,
                    mappings: rowMappings
                };

                const drillGrid = new DrillGrid(
                    drillGridDefn,
                    new HierarchicalAxis(colData),
                    new HierarchicalAxis(rowData));

                vm.visibility.chart = ! drillGrid.isEmpty();
                vm.drillGrid = drillGrid;
            });
    };

    vm.$onInit = () => {
        const promises = [
            serviceBroker
                .loadViewData(CORE_API.ApplicationStore.findBySelector, [ mkSelectionOptions(vm.parentEntityRef) ])
                .then(r => r.data),
            serviceBroker
                .loadAppData(CORE_API.DrillGridDefinitionStore.findAll)
                .then(r => r.data),
            settingsService
                .findOrDefault(DRILL_GRID_DEFAULT_DEFINITION_ID)
        ];

        $q.all(promises)
            .then(([apps, definitions, defaultDefinitionId]) => {
                if (_.isEmpty(definitions)) {
                    console.error('Cannot show drill grid if no definitions have been declared.')
                    return;
                }
                vm.allApps = apps;

                // use default, fall back to first if not found
                vm.selectedDefinition = _.find(definitions, { id: +defaultDefinitionId }) || definitions[0];
                loadChartData(vm.selectedDefinition);
            });
    };

    vm.onSelectDefinition = (d) => {
        vm.selectedDefinition = d;
        loadChartData(d);
    }
}


controller.$inject = [
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
    id: 'waltzDrillGridPanel',
    component
}