import {derived, writable} from "svelte/store";
import _ from "lodash";
import {
    mkLocalStorageFilterKey,
    mkRowFilter,
    prepareTableData,
    refreshSummaries,
    sameColumnRef
} from "./report-grid-utils";


export const selectedGrid = writable(null);
export const filters = writable([]);
export const columnDefs = writable([]);
export const derivedColumnDefs = writable([]);
export const selectedColumn = writable(null);
export const lastMovedColumn = writable(null);
export const ownedReportIds = writable([]);

export let columnsChanged = derived([columnDefs, selectedGrid], ([$columnDefs, $selectedGrid]) => {

    const originalColumnDefs = _.concat(
        $selectedGrid?.definition.fixedColumnDefinitions,
        $selectedGrid?.definition.derivedColumnDefinitions) || [];

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

export let additionalColumnOptionsChanged = derived(
    columnDefs,
    ($columnDefs) => _.some($columnDefs, d => d.additionalColumnOptionsChanged));

export let displayNameChanged = derived(
    columnDefs,
    ($columnDefs) => _.some($columnDefs, d => d.displayNameChanged));

export let positionChanged = derived(
    columnDefs,
    ($columnDefs) => _.some($columnDefs, d => d.originalPosition && d.originalPosition !== d.position));

export let derivationScriptChanged = derived(
    columnDefs,
    ($columnDefs) => _.some($columnDefs, d => d.derivationScriptChanged));

export let externalIdChanged = derived(
    columnDefs,
    ($columnDefs) => _.some($columnDefs, d => d.externalIdChanged));

export let hasChanged = derived(
    [columnsChanged, additionalColumnOptionsChanged, displayNameChanged, positionChanged, derivationScriptChanged, externalIdChanged],
    ([$columnsChanged, $additionalColumnOptionsChanged, $displayNameChanged, $positionChanged, $derivationScriptChanged, $externalIdChanged]) => {
        return $columnsChanged || $additionalColumnOptionsChanged || $displayNameChanged || $positionChanged || $derivationScriptChanged || $externalIdChanged;
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

        const columnDefinitions = _.concat(
            $selectedGrid?.definition.fixedColumnDefinitions,
            $selectedGrid?.definition.derivedColumnDefinitions);

        return refreshSummaries(
            workingTableData,
            columnDefinitions,
            $selectedGrid?.instance.ratingSchemeItems);
    }
});


function createActiveSummariesStore() {
    const {subscribe, set, update} = writable([]);

    const add = (colRef) => {
        update((all) => _.concat(all, [colRef]));
    };

    const remove = (colRef) => {
        update((all) => all.filter((t) => t !== colRef));
    };

    return {
        subscribe,
        set,
        add,
        remove,
    }
}

export const activeSummaries = createActiveSummariesStore();


const saveSummariesToLocalStorage = derived([selectedGrid, activeSummaries], ([$selectedGrid, $activeSummaries]) => {

    if (!$selectedGrid){
        return;
    }

    const key = mkLocalStorageFilterKey($selectedGrid?.definition.id);
    localStorage.setItem(key, JSON.stringify($activeSummaries));
})
.subscribe(() => {});