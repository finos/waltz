import template from "./report-grid-view-panel.html";
import {initialiseData} from "../../../common";
import {mkLinkGridCell} from "../../../common/grid-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {mkChunks} from "../../../common/list-utils";
import {determineForegroundColor, lightGrey} from "../../../common/colors";
import {rgb} from "d3-color";

const bindings = {
    parentEntityRef: "<",
    gridId: "<"
};

const initData = {
    categoryExtId: "CLOUD_READINESS",
    selectedCounterId: null,
    activeSummaryColRefs: [],
    filters: []
};

const nameCol = mkLinkGridCell("Name", "application.name", "application.id", "main.app.view", { pinnedLeft:true, width: 200});
const extIdCol = { field: "application.externalId", displayName: "Ext. Id", width: 100, pinnedLeft:true};

const unknownRating = {
    id: -1,
    color: lightGrey,
    description: "This rating has not been provided",
    name: "Unknown",
    rating: "Z",
    position: 0
};


function mkPropNameForRef(ref) {
    return `${ref.kind}_${ref.id}`;
}


function mkPropNameForCellRef(x) {
    return `${x.columnEntityKind}_${x.columnEntityId}`;
}


function initialiseDataForRow(application, columnRefs) {
    return _.reduce(
        columnRefs,
        (acc, c) => {
            acc[c] = unknownRating;
            return acc;
        },
        {application});
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
                 ng-style="{'background-color': COL_FIELD.color, 'color': COL_FIELD.fontColor}">
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
    const ratingSchemeItemsById = _
        .chain(gridData.instance.ratingSchemeItems)
        .map(d => {
            const c = rgb(d.color);
            return Object.assign({}, d, { fontColor: determineForegroundColor(c.r, c.g, c.b)})
        })
        .keyBy(d => d.id)
        .value();

    const colDefs = _.get(gridData, ["definition", "columnDefinitions"], []);
    const columnRefs = _.map(colDefs, c => mkPropNameForRef(c.columnEntityReference));

    return _
        .chain(gridData.instance.cellData)
        .groupBy(d => d.applicationId)
        .map((xs, k) => _.reduce(
            xs,
            (acc, x) => {
                acc[mkPropNameForCellRef(x)] = ratingSchemeItemsById[x.ratingId];
                return acc;
            },
            initialiseDataForRow(appsById[k], columnRefs)))
        .orderBy(d => d.application.name)
        .value();
}


/**
 * We are not interested in some properties in the table data.
 * @param k
 * @returns {boolean}
 */
function isSummarisableProperty(k) {
    return ! (k === "application"
        || k === "$$hashKey"
        || k === "visible");
}


function refreshSummaries(tableData, columnDefinitions, ratingSchemeItems) {

    // increments a pair of counters referenced by `prop` in the object `acc`
    const accInc = (acc, prop, visible) => {
        const counts = _.get(acc, prop, {visible: 0, total:  0});
        counts.total++;
        if (visible) {
            counts.visible++;
        }
        acc[prop] = counts;
    };

    // reduce each value in an object representing a row by incrementing counters based on the property / value
    const reducer = (acc, row) => {
        _.forEach(
            row,
            (v, k) => isSummarisableProperty(k)
                ? accInc(
                    acc,
                    k + "#" + v.id,
                    _.get(row, ["visible"], true))
                : acc);
        return acc;
    };

    const ratingSchemeItemsById = _.keyBy(ratingSchemeItems, d => d.id);
    const columnsByRef = _.keyBy(columnDefinitions, d => mkPropNameForRef(d.columnEntityReference));

    return _
        .chain(tableData)
        .reduce(reducer, {})  // transform into a raw summary object for all rows
        .map((counts, k) => { // convert basic prop-val/count pairs in the summary object into a list of enriched objects
            const [colRef, ratingId] = _.split(k, "#");
            return {counterId: k, counts, colRef, rating: _.get(ratingSchemeItemsById, ratingId, unknownRating)};
        })
        .groupBy(d => d.colRef)  // group by the prop (colRef)
        .map((counters, colRef) => ({ // convert each prop group into a summary object with the actual column and a sorted set of counters
            column: columnsByRef[colRef],
            counters: _.orderBy(  // sort counters according to the rating ordering
                counters,
                [
                    c => c.rating.position,
                    c => c.rating.name
                ]),
            total: _.sumBy(counters, c => c.counts.total),
            totalVisible: _.sumBy(counters, c => c.counts.visible)
        }))
        .orderBy([  // order the summaries so they reflect the column order
            d => d.column.position,
            d => d.column.columnEntityReference.name
        ])
        .value();
}


/**
 * Returns a function which acts as a predicate to test rows against.
 *
 * The set of filters is first grouped by the row property they test.
 * For a row to pass, _at least_ one filter for _each_ group (prop)
 * needs to pass.
 *
 * @param filters
 * @returns {function(*=): boolean}
 */
function mkRowFilter(filters = []) {
    const filtersByPropName = _.groupBy(filters, f => f.propName);
    return td => _.every(
        filtersByPropName,
        (filtersForProp, prop) => {
            const propRating = _.get(td, [prop, "id"], null);
            return _.some(filtersForProp, f => propRating === f.ratingId);
        });
}


function controller(serviceBroker) {

    const vm = initialiseData(this, initData);

    function refresh(filters = []) {
        vm.columnDefs = _.map(vm.allColumnDefs, cd => Object.assign(cd, {menuItems: [
            {
                title: "Add to summary",
                icon: "ui-grid-icon-info-circled",
                action: function() {
                    vm.onAddSummary(cd);
                },
                context: vm
            }
        ]}));

        const rowFilter = mkRowFilter(filters);

        const workingTableData =  _.map(
            vm.allTableData,
            d => Object.assign({}, d, { visible: rowFilter(d) }));

        vm.tableData = _.filter(workingTableData, d => d.visible);

        const summaries = refreshSummaries(
            workingTableData,
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

        vm.selectionOptions = mkSelectionOptions(vm.parentEntityRef);

        vm.loading = true;
        serviceBroker
            .loadViewData(
                CORE_API.ReportGridStore.getViewById,
                [vm.gridId, mkSelectionOptions(vm.parentEntityRef)])
            .then(r => {
                vm.filters = [];
                vm.loading = false;
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

    vm.onToggleFilter = (counter) => {
        if (_.some(vm.filters, f =>f.counterId === counter.counterId)) {
            vm.filters = _.reject(vm.filters, f =>f.counterId === counter.counterId);
            refresh(vm.filters);
        } else {
            const newFilter = {
                counterId: counter.counterId,
                propName: counter.colRef,
                ratingId: counter.rating.id
            };
            vm.filters = _.concat(vm.filters, [newFilter]);
            refresh(vm.filters);
        }
    };

    vm.onRemoveSummary = (summary) => {
        const refToRemove = mkPropNameForRef(summary.column.columnEntityReference);
        vm.activeSummaryColRefs = _.reject(vm.activeSummaryColRefs, ref => ref === refToRemove);
        // remove any filters which refer to the property used by this summary
        vm.filters = _.reject(vm.filters, f => f.propName === refToRemove);
        refresh(vm.filters);
    };

    vm.onAddSummary = (c) => {
        const colRef = mkPropNameForRef(c.columnDef.columnEntityReference);
        vm.activeSummaryColRefs = _.concat(vm.activeSummaryColRefs, [colRef]);
        refresh(vm.filters);
    };

    vm.isSelectedCounter = (cId) => {
        return _.some(vm.filters, f =>f.counterId === cId);
    };
}

controller.$inject = ["ServiceBroker"];

const component = {
    controller,
    bindings,
    template
};

export default {
    id: "waltzReportGridViewPanel",
    component,
}