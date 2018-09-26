import template from "./roadmap-scenario-diagram.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {prepareData} from "../../../scenario/components/scenario-diagram/scenario-diagram-data-utils";
import {initialiseData} from "../../../common";

const bindings = {
    scenarioId: "<",
    onCancel: "<"
};


const component = {
    bindings,
    template,
    controller
};

const initialState = {

};


function controller($q, serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const scenarioPromise = serviceBroker
            .loadViewData(CORE_API.ScenarioStore.getById, [ vm.scenarioId ])
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
            .then(() => vm.vizData = prepareData(vm.scenarioDefn, vm.applications, vm.measurables));
    };

}

controller.$inject = [
    "$q",
    "ServiceBroker"
];

const id = "waltzRoadmapScenarioDiagram";

export default {
    id,
    component
}


