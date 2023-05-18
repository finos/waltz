import {derived, get, writable} from "svelte/store";
import _ from "lodash";
import {$http} from "../../../common/WaltzHttp"
import {
    combineColDefs,
    determineDefaultColumnOptions,
    mkRowFilter,
    prepareTableData,
    refreshSummaries,
    sameColumnRef
} from "./report-grid-utils";
import {move} from "../../../common/list-utils";

const selectionOptions = writable(null);
const gridDefinition = writable(null);
const gridInstance = writable(null);
const gridMembers = writable([]);
const hasDirtyColumns = writable(false);
const userRole = writable("VIEWER");

const columnDefs = writable([]);
const filters = writable([]);
const activeSummaries = writable([]);
const preparedTableData = writable([]);

function selectGrid(gridId, opts) {

    reset();
    selectionOptions.set(opts);

    return $http
        .post(`api/report-grid/view/id/${gridId}`, opts)
        .then(d => {
            const cols = combineColDefs(d.data.definition);
            gridDefinition.set(d.data.definition);
            gridInstance.set(d.data.instance);
            gridMembers.set(d.data.members);
            columnDefs.set(cols);
            userRole.set(d.data.userRole);
            hasDirtyColumns.set(false);
            preparedTableData.set(prepareTableData(d.data.instance, cols));
            return d.data;
        });
}


function updateMember(email, role) {

    const cmd = {
        gridId: getGridId(),
        userId: email,
        role
    }

    return $http
        .post("api/report-grid-member/update", cmd)
        .then(reloadMembers);
}


function getGridId() {
    const defn = get(gridDefinition);

    if (_.isNil(defn)) {
        throw "Cannot get grid id before grid is loaded";
    }

    return defn.id;
}

function reloadMembers() {
    return $http
        .get(`api/report-grid-member/grid-id/${getGridId()}`)
        .then(d => gridMembers.set(d.data));
}

function removeMember(email) {
    const cmd = {
        gridId: getGridId(),
        userId: email,
    }

    return $http
        .post("api/report-grid-member/delete", cmd)
        .then(reloadMembers)
}


function addFixedColumn(d) {
    const columnEntityKind = _.get(d, "columnEntityKind");

    const extras = {
        kind: "REPORT_GRID_FIXED_COLUMN_DEFINITION",
        additionalColumnOptions: determineDefaultColumnOptions(columnEntityKind).key
    };

    const column = Object.assign({}, d, extras);

    _addColumn(column);
}


function addDerivedColumn(d) {
    const extras = {
        kind: "REPORT_GRID_DERIVED_COLUMN_DEFINITION",
        columnEntityKind: "REPORT_GRID_DERIVED_COLUMN_DEFINITION",
    };

    const column = Object.assign({}, d, extras);

    _addColumn(column);
}

function removeColumn(column) {
    hasDirtyColumns.set(true);
    columnDefs.update((cols) => _.reject(
        cols,
        r => sameColumnRef(r, column)));
}

function _addColumn(column) {
    columnDefs.update((cols) => {
        if (_.some(cols, c => sameColumnRef(column, c))) {
            return cols;
        } else {
            hasDirtyColumns.set(true);
            const maxPos = _.isEmpty(cols)
                ? 0
                : _.max(_.map(cols, d => d.position));
            return _.concat(
                cols,
                Object.assign({}, column, {position: maxPos + 1}));
        }
    });
}

function moveColumn(column, positionCount) {
    hasDirtyColumns.set(true);
    columnDefs.update((cols) => {
        const reorderedList = move(cols, _.indexOf(cols, column), positionCount);
        return _.map(reorderedList, (d, i) => Object.assign({}, d, {position: i}));
    })
}

function recalcPositions(reorderedList) {
    return _.map(reorderedList, (d, i) => Object.assign({}, d, {position: i}));
}

function moveColumnToStart(column) {
    hasDirtyColumns.set(true);
    columnDefs.update((cols) => {
        const columnTail = _.reject(cols, column);
        const reorderedColumns = _.concat([column], columnTail);
        return recalcPositions(reorderedColumns);
    })
}


function moveColumnToEnd(column) {
    hasDirtyColumns.set(true);
    columnDefs.update((cols) => {
        const columnHead = _.reject(cols, column);
        const reorderedColumns = _.concat(columnHead, [column]);
        return recalcPositions(reorderedColumns);
    })

}

function saveColumns() {
    const gridId = getGridId();
    const columns = get(columnDefs);
    const selectionOpts = get(selectionOptions);

    const fixedColumnDefinitions = _.filter(columns, d => d.kind === "REPORT_GRID_FIXED_COLUMN_DEFINITION");
    const derivedColumnDefinitions = _.filter(columns, d => d.kind === "REPORT_GRID_DERIVED_COLUMN_DEFINITION");

    return $http
        .post(`api/report-grid/id/${gridId}/column-definitions/update`, {
            fixedColumnDefinitions,
            derivedColumnDefinitions
        })
        .then(() => selectGrid(gridId, selectionOpts));
}


function reset() {
    selectionOptions.set(null);
    gridDefinition.set(null);
    gridInstance.set(null);
    gridMembers.set([]);
    preparedTableData.set([]);
    columnDefs.set([]);
    userRole.set("VIEWER");
    hasDirtyColumns.set(false);
    activeSummaries.set([]);
    filters.set([]);
}


function toggleSummary(summary) {
    activeSummaries.update((summaries) => {
        const columnId = summary.column.id;
        if (_.includes(summaries, columnId)) {

            filters.update((filters) => _.reject(filters, f => f.columnDefinitionId === summary.column.gridColumnId));

            return _.filter(summaries, (t) => t !== columnId);
        } else {
            return _.concat(summaries, [columnId]);
        }
    })
}

function toggleFilter(optionSummary) {

    filters.update((filters) => {

        const sId = optionSummary.summaryId;

        const isCurrentlySelected = _.some(
            filters,
            f => f.summaryId === sId);

        if (isCurrentlySelected) {
            return _.reject(
                filters,
                f => f.summaryId === sId);
        } else {
            const newFilter = {
                summaryId: sId,
                columnDefinitionId: optionSummary.columnDefinitionId,
                optionCode: optionSummary.optionInfo.code
            };
            return _.concat(filters, [newFilter]);
        }
    });
}

function updateColumnDetails(changedColumn) {
    hasDirtyColumns.set(true);
    columnDefs.update((cols) => {
        const columnsWithoutCol = _.reject(cols, d => sameColumnRef(d, changedColumn));
        return _.concat(columnsWithoutCol, changedColumn);
    });
}

function resetColumnDetails(changedColumn) {
    columnDefs.update((cols) => {
        const defn = get(gridDefinition);
        const originalColumnDefs = combineColDefs(defn);
        const originalColumn = _.find(originalColumnDefs, d => sameColumnRef(d, changedColumn));
        const columnsWithoutCol = _.reject(cols, d => sameColumnRef(d, changedColumn));
        return _.concat(columnsWithoutCol, originalColumn);
    });
}

const tableData = derived(
    [preparedTableData, filters],
    ([$preparedTableData, $filters]) => {
        if ($preparedTableData) {
            const rowFilter = mkRowFilter($filters);
            return _.map($preparedTableData, d => Object.assign({}, d, {visible: rowFilter(d)}));
        } else {
            return [];
        }
    });

const summaries = derived(
    [columnDefs, tableData],
    ([$columnDefs, $tableData]) => {

        if (_.isEmpty($columnDefs)) {
            return [];
        } else {
            return refreshSummaries(
                $tableData,
                $columnDefs);
        }
    });

function createStores() {

    return {
        columnDefs: {subscribe: columnDefs.subscribe},
        filters: {subscribe: filters.subscribe},
        activeSummaries: {subscribe: activeSummaries.subscribe},
        gridDefinition: {subscribe: gridDefinition.subscribe},
        gridInstance: {subscribe: gridInstance.subscribe},
        gridMembers: {subscribe: gridMembers.subscribe},
        hasDirtyColumns: {subscribe: hasDirtyColumns.subscribe},
        summaries: {subscribe: summaries.subscribe},
        tableData: {subscribe: tableData.subscribe},
        userRole: {subscribe: userRole.subscribe},
        selectGrid,
        updateMember,
        removeMember,
        addFixedColumn,
        addDerivedColumn,
        removeColumn,
        saveColumns,
        moveColumn,
        moveColumnToStart,
        moveColumnToEnd,
        toggleSummary,
        toggleFilter,
        updateColumnDetails,
        resetColumnDetails,
        reset
    };
}

export const gridService = createStores();