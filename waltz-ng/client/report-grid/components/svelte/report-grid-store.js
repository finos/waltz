import {derived, writable} from "svelte/store";
import _ from "lodash";
import {mkRowFilter, prepareTableData, refreshSummaries, sameColumnRef} from "./report-grid-utils";
import {activeSummaries} from "./report-grid-filters-store";

export const selectedGrid = writable(null);
export let filters = writable([]);
export let columnDefs = writable([]);
export let selectedColumn = writable(null);
export let lastMovedColumn = writable(null);
export let ownedReportIds = writable([]);

export let columnsChanged = derived([columnDefs, selectedGrid], ([$columnDefs, $selectedGrid]) => {

    const originalColumnDefs = $selectedGrid?.definition.columnDefinitions || [];

    if (!$selectedGrid) {
        return false
    } else if (_.size($columnDefs) !== _.size(originalColumnDefs)) {
        return true;
    } else {
        const sharedColumns = _.intersectionWith(
            originalColumnDefs,
            $columnDefs,
            sameColumnRef);
        return _.size(sharedColumns) !== _.size(originalColumnDefs) || _.size(sharedColumns) !== _.size($columnDefs);
    }
})

export let usageKindChanged = derived(columnDefs, ($columnDefs) => {
    return _.some($columnDefs, d => d.usageKindChanged)
});

export let ratingRollupRuleChanged = derived(columnDefs,
    ($columnDefs) => _.some($columnDefs, d => d.ratingRollupRuleChanged));

export let displayNameChanged = derived(columnDefs,
    ($columnDefs) => _.some($columnDefs, d => d.displayNameChanged));

export let positionChanged = derived(columnDefs, ($columnDefs) => {
    return _.some($columnDefs, d => d.originalPosition && d.originalPosition !== d.position);
});

export let hasChanged = derived(
    [columnsChanged, usageKindChanged, ratingRollupRuleChanged, displayNameChanged, positionChanged],
    ([$columnsChanged, $usageKindChanged, $ratingRollupRuleChanged, $displayNameChanged, $positionChanged]) => {
        return $columnsChanged || $usageKindChanged || $ratingRollupRuleChanged || $displayNameChanged || $positionChanged;
    });

export const tableData = derived(
    [selectedGrid],
    ([$selectedGrid]) => {
        if ($selectedGrid) {
            return prepareTableData($selectedGrid);
        }
    });

export const summaries = derived([selectedGrid, filters, tableData], ([$selectedGrid, $filters, $tableData]) => {

    if (_.isEmpty($selectedGrid)) {
        return [];
    } else {
        const rowFilter = mkRowFilter($filters);

        const workingTableData = _.map(
            $tableData,
            d => Object.assign({}, d, {visible: rowFilter(d)}));

        return refreshSummaries(
            workingTableData,
            $selectedGrid?.definition.columnDefinitions,
            $selectedGrid?.instance.ratingSchemeItems);
    }
})