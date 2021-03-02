import template from './entity-relationship-view.html';
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {

};

const initialState = [];


const addToHistory = (historyStore, relationship) => {
    if (! relationship) { return; }
    historyStore.put(
        `${relationship.a.name} ${relationship.relationship} ${relationship.b.name}`,
        "ENTITY_RELATIONSHIP",
        "main.entity-relationship.view",
        { id: relationship.id });
};


function controller($stateParams,
                    historyStore,
                    serviceBroker,
                    dynamicSectionManager) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        vm.parentEntityRef = {
            kind: "ENTITY_RELATIONSHIP",
            id: $stateParams.id
        };

        dynamicSectionManager.initialise("ENTITY_RELATIONSHIP");

        serviceBroker
            .loadViewData(CORE_API.EntityRelationshipStore.getById, [vm.parentEntityRef.id])
            .then(r => {
                vm.relationship = r.data;
                addToHistory(historyStore, vm.relationship)
            });
    };
}


controller.$inject = [
    "$stateParams",
    "HistoryStore",
    "ServiceBroker",
    "DynamicSectionManager"
];

const component = {
    template,
    bindings,
    controller
};

export default {
    id: 'waltzEntityRelationshipView',
    component
}