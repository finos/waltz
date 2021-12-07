<script>

    import EntitySelector from "./EntitySelector.svelte";
    import _ from "lodash";
    import {mkRef, sameRef} from "../../../../common/entity-utils";
    import ReportGridColumnSummary from "./ReportGridColumnSummary.svelte";
    import {columnUsageKind, ratingRollupRule} from "../report-grid-utils";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {reportGridStore} from "../../../../svelte-stores/report-grid-store";
    import toasts from "../../../../svelte-stores/toast-store";
    import ColumnDetailsEditor from "./ColumnDetailsEditor.svelte";
    import NoData from "../../../../common/svelte/NoData.svelte";
    import {columnDefs, hasChanged, selectedColumn, lastMovedColumn} from "../report-grid-store";
    import ColumnRemovalConfirmation from "./ColumnRemovalConfirmation.svelte";

    export let gridId;
    export let onSave = () => console.log("Saved report grid");


    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT",
        DELETE: "DELETE"
    }

    let activeMode = Modes.VIEW;

    let canBeAdded;

    function onSelect(d) {
        const column = {
            columnEntityReference: mkRef(d.kind, d.id, d.name || d.questionText, d.description),
            usageKind: columnUsageKind.NONE.key,
            ratingRollupRule: ratingRollupRule.NONE.key,
            position: 0,
        }

        const newList = _.concat(column, $columnDefs);

        $columnDefs = _.map(
            newList,
            d => Object.assign(
                {},
                d,
                { position: _.indexOf(newList, d) }));
    }

    function deleteColumn(d) {
        $columnDefs = _.reject(
           $columnDefs,
            r => sameRef(r.columnEntityReference, d.columnEntityReference));
        cancel();
    }

    function deleteEntity(d) {
        $columnDefs = _.reject(
           $columnDefs,
            r => sameRef(r.columnEntityReference, d));
        cancel();
    }

    function saveColumnDefs(columns) {

        const columnDefs = _.map(
            columns,
            d => ({
                columnEntityReference: d.columnEntityReference,
                position: d.position,
                usageKind: d.usageKind,
                ratingRollupRule: d.ratingRollupRule,
                displayName: d.displayName
            }));

        return reportGridStore
            .updateColumnDefinitions(gridId, {columnDefinitions: columnDefs})
            .then(() => {
                onSave();
                toasts.success("Report grid columns updated successfully");
                $selectedColumn = null;
                $lastMovedColumn = null;
                activeMode = Modes.VIEW;
            })
            .catch(() => toasts.error("Unable to update report grid"));
    }

    $: canBeAdded = (d) => {
        return !_.some(
            $columnDefs,
            r => sameRef(r.columnEntityReference, d));
    }

    function editColumn(column){
        $selectedColumn = column;
        activeMode = Modes.EDIT;
    }

    function removeColumn(column){
        $selectedColumn = column;
        activeMode = Modes.DELETE;
    }

    function cancel() {
        $selectedColumn = null;
        activeMode = Modes.VIEW
    }

</script>

<div class="help-block small"
     style="margin-bottom: 1em">
        <Icon name="info-circle"/>The columns for this report grid are listed below. You can add, remove or edit the column details.
        Use the arrows to change the order of the columns in the report grid.
</div>

{#if $hasChanged}
    <NoData type="warning">
        The columns for this report grid have been modified, to keep the changes please
        <button class="btn btn-skinny"
                on:click={() => saveColumnDefs($columnDefs)}>
            <Icon name="floppy-o"/>Save this report
        </button>
    </NoData>
{/if}

<div class="row">
    <div class="col-sm-4">
        <h5><Icon name="plus"/>Add a Column:</h5>
        <EntitySelector onSelect={onSelect}
                        onDeselect={deleteEntity}
                        selectionFilter={canBeAdded}/>
    </div>

    <div class="col-sm-4"
         style="padding-left: 0; padding-right: 0">
        <ReportGridColumnSummary {columnDefs}
                                 {gridId}
                                 onRemove={removeColumn}
                                 onEdit={editColumn}/>
    </div>

    <div class="col-sm-4">
        {#if activeMode === Modes.EDIT}
            <ColumnDetailsEditor column={$selectedColumn}
                                 onCancel={cancel}
                                 onRemove={removeColumn}/>
        {:else if activeMode === Modes.DELETE}
            <ColumnRemovalConfirmation column={$selectedColumn}
                                       onCancel={cancel}
                                       onRemove={deleteColumn}/>
        {:else if activeMode === Modes.VIEW}
            <div class="help-block small">
                <Icon name="info-circle"/>
                Select a column to edit from the list to the left
            </div>
        {/if}
    </div>
</div>