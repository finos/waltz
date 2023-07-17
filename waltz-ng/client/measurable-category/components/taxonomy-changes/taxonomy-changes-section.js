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
            field: "params",
            displayName: "Parameters",
            cellTemplate: `
                <div class="ui-grid-cell-contents">
                    <waltz-taxonomy-change-summary-cell change="row.entity">
                    </waltz-taxonomy-change-summary-cell>
                </div>`
        }, {
            field: "lastUpdatedBy",
            displayName: "Updated By",
            width: "10%",
        }, {
            field: "lastUpdatedAt",
            displayName: "Updated At",
            cellTemplate: "<div class=\"ui-grid-cell-contents\"><waltz-from-now timestamp=\"COL_FIELD\"></waltz-from-now></div>"
        }
    ]
};


const bindings = {
    parentEntityRef: "<"
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = (c) => {
        if (vm.parentEntityRef) {
            serviceBroker
                .execute(
                    CORE_API.TaxonomyManagementStore.findAllChangesByDomain,
                    [vm.parentEntityRef])
                .then(r => {
                    vm.items = _
                        .chain(r.data)
                        .filter(d => d.status === "EXECUTED")
                        .orderBy(
                            [d => d.lastUpdatedAt, d => d.createdAt],
                            ["desc", "desc"])
                        .value();
                    console.log(vm.items)
                });

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
