import template from "./assessment-definition-list.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkEntityLinkGridCell} from "../../../common/grid-utils";

const bindings = {};

const initialState = {
    definitions: [],
    columnDefs: [
        {
            field: "entityKind",
            cellFilter: "toDisplayName:'entity'",
            width: "10%"
        },
        { field: "name", width: "30%", cellTemplate: `<div class=''><a ui-sref="main.assessment-definition.view ({id: row.entity.id})" ng-bind="COL_FIELD"></a>` },
        { field: "externalId", width: "10%" },
        { field: "description", width: "50%" }
    ]
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



