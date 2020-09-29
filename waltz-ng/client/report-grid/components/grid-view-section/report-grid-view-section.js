import template from "./report-grid-view-section.html";
import {initialiseData} from "../../../common";
import {mkLinkGridCell} from "../../../common/grid-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {mkChunks} from "../../../common/list-utils";

const bindings = {
    parentEntityRef: "<"
};

const initData = {
    categoryExtId: "CLOUD_READINESS",
    selectedCounterId: null,
    activeSummaryColRefs: []
};

const nameCol = mkLinkGridCell("Name", "application.name", "application.id", "main.app.view", { pinnedLeft:true, width: 200});
const extIdCol = { field: "application.externalId", displayName: "Ext. Id", width: 100, pinnedLeft:true};


function mkPropNameForRef(ref) {
    return `${ref.kind}_${ref.id}`;
}


function mkPropNameForCellRef(x) {
    return `${x.columnEntityKind}_${x.columnEntityId}`;
}


function prepareColumnDefs(gridData) {
    const colDefs = _.get(gridData, ["definition", "columnDefinitions"], []);

    const measurableCols = _
        .chain(colDefs)
        .map(c => ({
            field: mkPropNameForRef(c.columnEntityReference),
            displayName: c.columnEntityReference.name,
            columnDef: c,
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
                acc[mkPropNameForCellRef(x)] = ratingSchemeItemsById[x.ratingId];
                return acc;
            },
            { application: appsById[k] }))
        .orderBy(d => d.application.name)
        .value();
}


function refreshSummaries(tableData, columnDefinitions, ratingSchemeItems) {
    const ratingSchemeItemsById = _.keyBy(ratingSchemeItems, d => d.id);
    const columnsByRef = _.keyBy(columnDefinitions, d => mkPropNameForRef(d.columnEntityReference));

    const accInc = (acc, prop) => _.set(acc, prop, _.get(acc, prop, 0) + 1);
    const reducer = (acc, row) => {
        _.forEach(
            row,
            (v, k) => k === "application" || k === "$$hashKey"
                ? acc
                : accInc(acc, k + "#"+ v.id));
        return acc;
    };

    return _
        .chain(tableData)
        .reduce(reducer, {})
        .map((count, k) => {
            const [colRef, ratingId] = _.split(k, "#");
            return {counterId: k, count, colRef, rating: ratingSchemeItemsById[ratingId]};
        })
        .groupBy(d => d.colRef)
        .map((counters, colRef) => ({ column: columnsByRef[colRef], counters }))
        .orderBy([
            d => d.column.position,
            d => d.column.columnEntityReference.name
        ])
        .value();
}


function controller(serviceBroker) {

    const vm = initialiseData(this, initData);

    function refresh(filterParams) {
        vm.columnDefs = _.map(vm.allColumnDefs, cd => Object.assign(cd, {menuItems: [
            {
                title: "Add to summary",
                icon: "ui-grid-icon-info-circled",
                action: function($event) {
                    vm.onAddSummary(cd);
                },
                context: vm
            }
        ]}));

        const rowFilter = td => {
            const assocRatingIdForRow = _.get(td, [filterParams.propName, "id"], null);
            return assocRatingIdForRow === filterParams.ratingId;
        };

        vm.tableData = filterParams
            ? _.filter(
                vm.allTableData,
                rowFilter)
            : vm.allTableData;

        const summaries = refreshSummaries(
            vm.tableData,
            vm.rawGridData.definition.columnDefinitions,
            vm.rawGridData.instance.ratingSchemeItems);

        vm.chunkedSummaryData = mkChunks(
            _.filter(
                summaries,
                d => _.includes(vm.activeSummaryColRefs, mkPropNameForRef(d.column.columnEntityReference))),
            4);
    }

    vm.$onChanges = () => {
        if (! vm.parentEntityRef) return;
        serviceBroker
            .loadViewData(
                CORE_API.ReportGridStore.getViewById,
                [1, mkSelectionOptions(vm.parentEntityRef)])
            .then(r => {
                vm.rawGridData = r.data;
                vm.allTableData = prepareTableData(vm.rawGridData);
                vm.allColumnDefs = prepareColumnDefs(vm.rawGridData);
                vm.activeSummaryColRefs = _
                    .chain(vm.rawGridData.definition.columnDefinitions)
                    .filter(d => d.usageKind === "SUMMARY")
                    .map(d => mkPropNameForRef(d.columnEntityReference))
                    .value();

                refresh();
            });
    };

    vm.blat = () => {
        vm.tableData = _.filter(vm.tableData, d => d.application.id % 2 === 0);
    };

    vm.onToggleFilter = (counter) => {
        if (vm.selectedCounter === counter.counterId) {
            vm.selectedCounter = null;
            refresh();
        } else {
            vm.selectedCounter = counter.counterId;
            const filterParams = {
                propName: counter.colRef,
                ratingId: counter.rating.id
            };
            refresh(filterParams);
        }
    };

    vm.onRemoveSummary = (summary) => {
        const refToRemove = mkPropNameForRef(summary.column.columnEntityReference);
        vm.activeSummaryColRefs = _.reject(vm.activeSummaryColRefs, ref => ref === refToRemove);
        refresh();
    };

    vm.onAddSummary = (c) => {
        const colRef = mkPropNameForRef(c.columnDef.columnEntityReference);
        vm.activeSummaryColRefs = _.concat(vm.activeSummaryColRefs, [colRef]);
        refresh();
    };
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