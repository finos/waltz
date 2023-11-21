import template from "./taxonomy-changes-section.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {mkEntityLinkGridCell} from "../../../common/grid-utils";


const initialState = {
    items: [],
    columnDefs: [
        {
            field: "changeType",
            cellFilter: "toDisplayName:'taxonomyChangeType'",
            width: "10%"
        },
        mkEntityLinkGridCell("Entity", "primaryReference", "none", "right"),
        {
            field: "value",
            displayName: "Value"
        }, {
            field: "originalValue",
            displayName: "Original Value"
        }, {
            field: "lastUpdatedBy",
            displayName: "Updated By",
            width: "10%",
        }, {
            field: "lastUpdatedAt",
            displayName: "Updated At",
            cellTemplate: `
                <div class="ui-grid-cell-contents">
                    <waltz-from-now timestamp="COL_FIELD">
                    </waltz-from-now>
                </div>`
        }
    ]
};


const bindings = {
    parentEntityRef: "<"
};


function determineValue(change) {
    const params = change.params;
    switch (change.changeType) {
        case "ADD_CHILD":
            return params.name;
        case "UPDATE_NAME":
            return params.name;
        case "UPDATE_DESCRIPTION":
            return params.description;
        case "UPDATE_CONCRETENESS":
            return params.concrete;
        case "UPDATE_EXTERNAL_ID":
            return params.externalId;
        case "MOVE":
            return `To: ${params.destinationName}`;
        case "MERGE":
            return `Into: ${params.targetName}`;
        case "REORDER_SIBLINGS":
            return params.listAsNames;
        default:
            return "-";
    }
}


function determineOriginalValue(change) {
    const params = change.params;
    const orig = params.originalValue || "-";
    switch (change.changeType) {
        case "MOVE":
            return `From: ${orig}`;
        default:
            return orig;
    }
}


function mkValueAndOriginalValueProps(change) {

    return {
        value: determineValue(change),
        originalValue: determineOriginalValue(change)
    };
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = (c) => {
        if (vm.parentEntityRef) {
            serviceBroker
                .execute(
                    CORE_API.TaxonomyManagementStore.findAllChangesByDomain,
                    [vm.parentEntityRef])
                .then(r => vm.items = _
                    .chain(r.data)
                    .filter(d => d.status === "EXECUTED")
                    .map(d => {
                        return Object.assign(
                            {
                                changeType: d.changeType,
                                primaryReference: d.primaryReference,
                                lastUpdatedBy: d.lastUpdatedBy,
                                lastUpdatedAt: d.lastUpdatedAt,
                                params: d.params
                            },
                            mkValueAndOriginalValueProps(d));
                    })
                    .orderBy(
                        [d => d.lastUpdatedAt, d => d.createdAt],
                        ["desc", "desc"])
                    .value());
        }
    };
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
    id: "waltzTaxonomyChangesSection",
    component
};
