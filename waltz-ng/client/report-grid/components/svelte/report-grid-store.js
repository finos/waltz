import {derived, writable} from "svelte/store";
import _ from "lodash";
import {mkRowFilter, prepareTableData, refreshSummaries} from "./report-grid-utils";
import {sameRef} from "../../../common/entity-utils";

export const selectedGrid = writable(null);
export let filters = writable([]);
export let activeSummaryColRefs = writable([]);
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
            (v1, v2) => sameRef(v1.columnEntityReference, v2.columnEntityReference))
        return _.size(sharedColumns) !== _.size(originalColumnDefs) || _.size(sharedColumns) !== _.size($columnDefs);
    }
})

export let usageKindChanged = derived(columnDefs, ($columnDefs) => {
    return _.some($columnDefs, d => d.usageKindChanged)
});

export let ratingRollupRuleChanged = derived(columnDefs,
    ($columnDefs) =>  _.some($columnDefs, d => d.ratingRollupRuleChanged));

export let displayNameChanged = derived([columnDefs, selectedGrid], ([$columnDefs, selectedGrid]) => {
    return _.some($columnDefs, d => d.usageKindChanged);
})

export let positionChanged = derived(columnDefs, ($columnDefs) => {
    return  _.some($columnDefs, d => d.originalPosition && d.originalPosition !== d.position);
});

export let hasChanged = derived(
    [columnsChanged, usageKindChanged, ratingRollupRuleChanged, positionChanged],
    ([$columnsChanged, $usageKindChanged, $ratingRollupRuleChanged, $positionChanged]) => {
        return $columnsChanged || $usageKindChanged || $ratingRollupRuleChanged || $positionChanged;
    })

export const summaries = derived([selectedGrid, filters], ([$selectedGrid, $filters]) => {

    if(_.isEmpty($selectedGrid)) {
        return []
    } else {
        const rowFilter = mkRowFilter($filters);

        const workingTableData =  _.map(
            prepareTableData($selectedGrid),
            d => Object.assign({}, d, { visible: rowFilter(d) }));

        return refreshSummaries(
            workingTableData,
            $selectedGrid?.definition.columnDefinitions,
            $selectedGrid?.instance.ratingSchemeItems);
    }
})
