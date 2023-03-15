import template from "./legal-entity-relationship-bulk-upload.html";
import {initialiseData} from "../../../common";
import BulkUploadLegalEntityRelationshipsPanel from "./BulkUploadLegalEntityRelationshipsPanel.svelte";


const bindings = {
    relationshipKind: "<",
    onDone: "<"
}


const initialState = {
    BulkUploadLegalEntityRelationshipsPanel
}


function controller($scope, serviceBroker) {

    const vm = initialiseData(this, initialState);

}


controller.$inject = [
    "$scope",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    id: "waltzLegalEntityRelationshipBulkUpload",
    component
};