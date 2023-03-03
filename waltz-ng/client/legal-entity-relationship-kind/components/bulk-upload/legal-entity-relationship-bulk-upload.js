import template from "./legal-entity-relationship-bulk-upload.html";
import {initialiseData} from "../../../common";
import BulkUploadLegalEntityRelationshipsPanel from "./BulkUploadLeagelEntityRelationshipsPanel.svelte";
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

    const unsub = resolvedRows.subscribe((d) => {
        $scope.$applyAsync(() => {
            vm.rows = d
        });
    });

    const unsubResp = resolveResponse.subscribe((d) => {
        $scope.$applyAsync(() => {

            vm.rows = _.map(d.rows, r => Object.assign(
                {},
                r,
                {cellsByColumn: _.keyBy(r.assessmentRatings, d => `COL_${d.columnId}`)}));

            console.log({rows: vm.rows});

            const assessmentColDefs = _.map(d.assessmentHeaders, d => mkAssessmentColumnDef(d));

            vm.columnDefs = _.concat(
                relColumnDefs,
                assessmentColDefs);

            console.log({cols: vm.columnDefs});
        });
    });

    const unsubMode = activeMode.subscribe((d) => {
        $scope.$applyAsync(() => vm.showGrid = d === Modes.RESOLVED);
    });

    vm.$onChanges = () => {
    }

    function mkAssessmentColumnDef(assessmentHeading) {
        return {
            field: "legalEntityRelationship",
            displayName: assessmentHeading.inputString,
            cellTemplate: `
            <div class="ui-grid-cell-contents">
            <pre ng-bind="COL_FIELD | json"></pre>
</div>`
        };
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

    const relColumnDefs = [
        mkEntityLinkColumnDef("Target Entity", "legalEntityRelationship.targetEntityReference.resolvedEntityReference"),
        mkEntityLinkColumnDef("Legal Entity", "legalEntityRelationship.legalEntityReference.resolvedEntityReference"),
        {
            field: "legalEntityRelationship.comment",
            displayName: "Comment"
        }];

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