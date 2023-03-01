import template from "./legal-entity-relationship-bulk-upload.html";
import {initialiseData} from "../../../common";
import BulkUploadLegalEntityRelationshipsPanel from "./BulkUploadLeagelEntityRelationshipsPanel.svelte";
import {resolvedRows, activeMode, Modes} from "./bulk-upload-relationships-store";


const bindings = {
    relationshipKind: "<"
}


const initialState = {
    BulkUploadLegalEntityRelationshipsPanel
}


function controller($scope, serviceBroker) {

    const vm = initialiseData(this, initialState);

    const unsub = resolvedRows.subscribe((d) => {
        $scope.$applyAsync(() => vm.rows = d);
    });

    const unsubMode = activeMode.subscribe((d) => {
        $scope.$applyAsync(() => vm.showGrid = d === Modes.RESOLVED);
    });

    vm.$onChanges = () => {
    }

    function mkEntityLinkColumnDef(columnHeading, entityRefField) {
        return {
            field: entityRefField + '.name',
            displayName: columnHeading,
            cellTemplate: `
            <div class="ui-grid-cell-contents">
                <waltz-entity-link ng-if="row.entity.${entityRefField}"
                                   entity-ref="row.entity.${entityRefField}">
                </waltz-entity-link>
            </div>`
        };
    }

    vm.columnDefs = [
        mkEntityLinkColumnDef("Target Entity", "legalEntityRelationship.targetEntityReference"),
        mkEntityLinkColumnDef("Legal Entity", "legalEntityRelationship.legalEntityReference"),
        {
            field: "legalEntityRelationship.comment",
            displayName: "Comment"
        },
        {
            field: "legalEntityRelationship.status",
            displayName: "Status"
        },
        {
            field: "legalEntityRelationship.errors",
            displayName: "Errors"
        }
    ];
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