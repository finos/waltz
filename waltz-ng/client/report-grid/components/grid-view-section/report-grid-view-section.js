import template from "./report-grid-view-section.html";
import {initialiseData} from "../../../common";
import {mkLinkGridCell} from "../../../common/grid-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";

const bindings = {
   parentEntityRef: "<"
};

const initData = {
    categoryExtId: "CLOUD_READINESS",
};

const nameCol = mkLinkGridCell("Name", "application.name", "application.id", "main.app.view", { pinnedLeft:true, width: 200});
const extIdCol = { field: "application.externalId", displayName: "Ext. Id", width: 100, pinnedLeft:true};


function prepareColumnDefs(gridData) {
    const colDefs = _.get(gridData, ["definition", "columnEntityReferences"], []);

    const measurableCols = _
        .chain(colDefs)
        .map(c => ({
            field: `${c.kind}_${c.id}`,
            displayName: c.name,
            width: 100,
            cellTemplate: `
            <div class="waltz-grid-color-cell"
                 ng-bind="COL_FIELD.name"
                 ng-style="{'background-color': COL_FIELD.color}">
            </div>`,
            sortingAlgorithm: (a, b) => {
                if (a == null) return 1;
                if (b == null) return -1;
                return a.position - b.position;
            }
        }))
        .value();

    return _.concat([nameCol, extIdCol], measurableCols);
}


function prepareTableData(gridData) {
    const appsById = _.keyBy(gridData.instance.applications, d => d.id);
    const ratingSchemeItemsById = _.keyBy(gridData.instance.ratingSchemeItems, d => d.id);
    return _
        .chain(gridData.instance.cellData)
        .groupBy(d => d.applicationId)
        .map((xs, k) => _.reduce(
            xs,
            (acc, x) => {
                acc[`${x.columnEntityKind}_${x.columnEntityId}`] = ratingSchemeItemsById[x.ratingId];
                return acc;
            },
            { application: appsById[k]}))
        .orderBy(d => d.application.name)
        .value();
}


function controller(serviceBroker) {

    const vm = initialiseData(this, initData);

    vm.$onChanges = () => {
        if (! vm.parentEntityRef) return;
        serviceBroker
            .loadViewData(
                CORE_API.ReportGridStore.getViewById,
                [1, mkSelectionOptions(vm.parentEntityRef)])
            .then(r => {
                const gridData = r.data;
                vm.columnDefs = prepareColumnDefs(gridData);
                vm.tableData = prepareTableData(gridData);
            });
    }
}

controller.$inject = ["ServiceBroker"];

const component = {
    controller,
    bindings,
    template
}

export default {
    id: "waltzReportGridViewSection",
    component,
}