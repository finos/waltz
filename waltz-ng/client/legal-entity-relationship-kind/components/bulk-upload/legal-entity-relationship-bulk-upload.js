import template from "./legal-entity-relationship-bulk-upload.html";
import {initialiseData} from "../../../common";
import BulkUploadLegalEntityRelationshipsPanel from "./BulkUploadLegalEntityRelationshipsPanel.svelte";
import {resolvedRows, activeMode, Modes, resolveResponse} from "./bulk-upload-relationships-store";
import _ from "lodash";


const bindings = {
    relationshipKind: "<"
}


const initialState = {
    BulkUploadLegalEntityRelationshipsPanel
}


function controller($scope, serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
    }

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