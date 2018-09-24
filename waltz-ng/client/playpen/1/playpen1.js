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


import _ from "lodash";
import template from "./playpen1.html";
import {CORE_API} from "../../common/services/core-api-utils";
import {initialiseData} from "../../common";
import {
    mkRandomMeasurable,
    mkRandomRowData,
    prepareData
} from "../../roadmap/components/scenario-diagram/scenario-diagram-data-utils";

const initData = {


};

function mkDemoData(colCount, rowCount) {
    return {
        rowData: _.times(rowCount, () => mkRandomRowData(colCount)),
        columnHeadings: _.times(colCount, i => mkRandomMeasurable(i, "col")),
        rowHeadings: _.times(rowCount, i => mkRandomMeasurable(i, "row"))
    }
}


function controller($element, $q, serviceBroker) {

    const vm = initialiseData(this, initData);


    vm.$onInit = () => {
        const scenarioPromise = serviceBroker
            .loadViewData(CORE_API.RoadmapStore.getScenarioById, [1, 1])
            .then(r => vm.scenarioDefn = r.data);

        const applicationPromise = scenarioPromise
            .then(() => _.map(vm.scenarioDefn.ratings, r => r.item.id))
            .then(appIds => serviceBroker.loadViewData(CORE_API.ApplicationStore.findByIds, [ appIds ]))
            .then(r => vm.applications = r.data);

        const measurablePromise = scenarioPromise
            .then(() => serviceBroker.loadAppData(CORE_API.MeasurableStore.findAll))
            .then(r => {
                const requiredMeasurableIds = _.map(vm.scenarioDefn.axisDefinitions, d => d.item.id);
                vm.measurables = _.filter(r.data, m => _.includes(requiredMeasurableIds, m.id));
            });

        $q.all([scenarioPromise, applicationPromise, measurablePromise])
            .then(() => vm.realData = prepareData(vm.scenarioDefn, vm.applications, vm.measurables));

        vm.demoData = mkDemoData(3, 4);
        global.vm = vm;
    }
}

controller.$inject = ["$element", "$q", "ServiceBroker"];

const view = {
    template,
    controller,
    controllerAs: "ctrl",
    bindToController: true,
    scope: {}
};

export default view;