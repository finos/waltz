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
import _ from "lodash";
import { CORE_API } from '../../common/services/core-api-utils';


/**
 * Measures have their own kinds.  Returning everything would be too much
 * @param all
 * @param id
 * @returns {*}
 */
function filterBySameMeasurableCategory(all, id) {
    const measurable = _.find(all, { id });
    return measurable
        ? _.filter(all, m => m.categoryId === measurable.categoryId)
        : all;
}


function utils(serviceBroker) {

    const findAllForKind = (kind, id /* optional */) => {
        switch (kind) {
            case 'APP_GROUP':
                return serviceBroker
                    .loadViewData(CORE_API.AppGroupStore.getById, [id])
                    .then(r => r.data);
            case 'MEASURABLE':
                return serviceBroker
                    .loadAppData(CORE_API.MeasurableStore.findAll, [])
                    .then(r => filterBySameMeasurableCategory(r.data, id));
            case 'ORG_UNIT':
                return serviceBroker
                    .loadAppData(CORE_API.OrgUnitStore.findAll, [])
                    .then(r => r.data);
            case 'FLOW_DIAGRAM':
                return serviceBroker
                    .loadViewData(CORE_API.FlowDiagramStore.getById, [id])
                    .then(r => [r.data]);
            case 'CHANGE_INITIATIVE':
                return serviceBroker
                    .loadViewData(CORE_API.ChangeInitiativeStore.getById, [id])
                    .then(r => [r.data]);
            case 'SCENARIO':
                return serviceBroker
                    .loadViewData(CORE_API.ScenarioStore.getById, [id])
                    .then(r => serviceBroker
                        .loadViewData(CORE_API.ScenarioStore.findForRoadmap, [r.data.scenario.roadmapId]))
                    .then(r => r.data);
            case 'ALL':
                return Promise.resolve([{kind: 'ALL', id: 1, name: "All"}]);
            default :
                throw `esu: Cannot create hierarchy for kind - ${kind}`;
        }
    };

    return {
        findAllForKind
    };
}


utils.$inject = [
    'ServiceBroker'
];


export default utils;
