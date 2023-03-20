import template from "./legal-entity-relationship-bulk-upload.html";
import {initialiseData} from "../../../common";
import BulkUploadLegalEntityRelationshipsPanel from "./BulkUploadLegalEntityRelationshipsPanel.svelte";


const bindings = {
    relationshipKindId: "<",
    onDone: "<"
}


const initialState = {
    BulkUploadLegalEntityRelationshipsPanel
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
    };
}


controller.$inject = [
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