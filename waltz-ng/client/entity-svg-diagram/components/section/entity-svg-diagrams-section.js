import {initialiseData} from "../../../common/index";
import {CORE_API} from '../../../common/services/core-api-utils';
import template from "./entity-svg-diagrams-section.html";


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    diagrams: [],
    visibility: {}
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(CORE_API.EntitySvgDiagramStore.findByEntityReference, [vm.parentEntityRef])
            .then(result => {
                vm.diagrams = result.data;
                vm.visibility.tab = vm.diagrams.length > 0 ? vm.diagrams[0].id : null;
            });
    };

}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzEntitySvgDiagramsSection'
};
