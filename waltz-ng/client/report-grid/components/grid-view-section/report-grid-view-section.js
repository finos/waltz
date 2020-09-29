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


function prepareTableData(gridData, filterParams = null) {
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
            { application: appsById[k]}))
        .filter(row => filterParams
            ? _.get(row, [filterParams.propName, "id"], -1) === filterParams.ratingId
            : true)
        .orderBy(d => d.application.name)
        .value();
}


function prepareSummaries(gridData) {
    const summarisedColsByRef = _.chain(gridData.definition.columnDefinitions)
        .filter(cd => cd.usageKind === "SUMMARY")
        .keyBy(cd => mkPropNameForRef(cd.columnEntityReference))
        .value();
    const ratingSchemeItemsById = _.keyBy(gridData.instance.ratingSchemeItems, d => d.id);
    const cellData = _.get(gridData, ["instance", "cellData"], []);

    const summaries = _
        .chain(cellData)
        .filter(d => _.has(summarisedColsByRef, mkPropNameForCellRef(d)))
        .groupBy(d => mkPropNameForCellRef(d))
        .mapValues(dataForSingleCol => _.countBy(dataForSingleCol, d => d.ratingId))
        .mapValues(ratingCountsForSingleCol => _.map(
            ratingCountsForSingleCol,
            (count, k) => ({
                rating: ratingSchemeItemsById[k],
                count
            })))
        .map((counts, colRef) => ({
            columnEntityReference: _.get(summarisedColsByRef, [colRef, "columnEntityReference"], null),
            position: _.get(summarisedColsByRef, [colRef, "position"], 0),
            counts: _.map(counts, c => Object.assign(c, { counterId: colRef + "_" + c.rating.id }))
        }))
        .orderBy([d => d.position, d => d.columnEntityReference.name])
        .value();

    return mkChunks(summaries, 4);
}

function controller(serviceBroker) {

    const vm = initialiseData(this, initData);

    function prepareData(gridData, filterParams = null) {
        vm.columnDefs = _.map(prepareColumnDefs(gridData), cd => Object.assign(cd, {menuItems: [
            {
                title: "Add to summary",
                icon: "ui-grid-icon-info-circled",
                action: function($event) {
                    console.log($event, cd)
                    this.context.blat(); // $scope.blargh() would work too, this is just an example
                },
                context: vm
            }
        ]}));

        vm.tableData = prepareTableData(gridData, filterParams);
        vm.summaryRows = prepareSummaries(gridData, filterParams);
    }

    vm.$onChanges = () => {
        if (! vm.parentEntityRef) return;
        serviceBroker
            .loadViewData(
                CORE_API.ReportGridStore.getViewById,
                [1, mkSelectionOptions(vm.parentEntityRef)])
            .then(r => {
                vm.gridData = r.data;
                prepareData(vm.gridData);
            });
    };

    vm.blat = () => {
        vm.tableData = _.filter(vm.tableData, d => d.application.id % 2 === 0);
    };

    vm.onToggleFilter = (colRef, counter) => {
        if (vm.selectedCounter === counter.counterId) {
            vm.selectedCounter = null;
            prepareData(vm.gridData);
        } else {
            vm.selectedCounter = counter.counterId;
            const filterParams = {
                propName: mkPropNameForRef(colRef),
                ratingId: counter.rating.id
            };
            prepareData(vm.gridData, filterParams);
        }
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