import template from "./assessment-definition-list.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";

const bindings = {};

const initialState = {
    definitions: []
};


function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.AssessmentDefinitionStore.findAll)
            .then(r => vm.definitions = _.sortBy(r.data, [d => d.entityKind, d => d.name]));
    }

}
controller.$inject = [
    "ServiceBroker"
];


const component = {
    bindings,
    controller,
    template
};


export default {
    id: "waltzAssessmentDefinitionList",
    component
};



