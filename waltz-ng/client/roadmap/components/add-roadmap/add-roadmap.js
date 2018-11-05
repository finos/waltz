import {initialiseData, notEmpty} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {toEntityRef} from "../../../common/entity-utils";

import template from "./add-roadmap.html";


const bindings = {
    onAddRoadmap: "<",
    onCancel: "<"
};


const initialState = {};


function toCommand(model) {
    if (!validateModel(model)) return null;

    return {
        name: model.name,
        rowType: toEntityRef(model.rowType),
        columnType: toEntityRef(model.columnType),
        ratingSchemeId: model.ratingScheme.id
    };
}


function validateModel(model) {
    return notEmpty(model.name) &&
        notEmpty(model.columnType) &&
        notEmpty(model.rowType) &&
        notEmpty(model.ratingScheme);
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(
                CORE_API.MeasurableCategoryStore.findAll)
            .then(r => {
                vm.axisOptions = r.data;
            });

        serviceBroker
            .loadAppData(
                CORE_API.RatingSchemeStore.findAll)
            .then(r => {
                vm.ratingSchemes = r.data;
            });
    };

    vm.onFormChange = () => {
        vm.preventSubmit = !validateModel(vm.model);
    };

    vm.onAdd = () => {
        const command = toCommand(vm.model);
        return vm.onAddRoadmap(command);
    };
}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    controller,
    template,
    bindings
};


export default {
    id: "waltzAddRoadmap",
    component
};