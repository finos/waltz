<script>
    import EntitySelector from "./EntitySelector.svelte";
    import _ from "lodash";
    import ReportGridColumnSummary from "./ReportGridColumnSummary.svelte";
    import {sameColumnRef} from "../report-grid-utils";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import toasts from "../../../../svelte-stores/toast-store";
    import FixedColumnDetailsEditor from "./FixedColumnDetailsEditor.svelte";
    import DerivedColumnDetailsEditor from "./DerivedColumnDetailsEditor.svelte";
    import NoData from "../../../../common/svelte/NoData.svelte";
    import {activeSummaries, filters} from "../report-grid-store";
    import ColumnRemovalConfirmation from "./ColumnRemovalConfirmation.svelte";
    import Markdown from "../../../../common/svelte/Markdown.svelte";
    import {derivedColumnHelpText} from "./column-definition-utils";
    import {gridService} from "../report-grid-service";


    export let gridId;
    export let onSave = () => console.log("Saved report grid");

    const {columnDefs, hasDirtyColumns, gridDefinition} = gridService;

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT",
        DELETE: "DELETE",
        DERIVED: "DERIVED"
    }

    let activeMode = Modes.VIEW;

    let canBeAdded;
    let selectedColumn;

    let workingDerivedCol = {
        displayName: null,
        derivationScript: null,
        columnDescription: null
    };

    function clearWorking() {
        workingDerivedCol = {
            displayName: null,
            derivationScript: null,
            externalId: null,
            columnDescription: null
        };
    }

    function onSelect(d) {
        gridService.addFixedColumn(d);
    }

    function addDerivedColumn() {
        gridService.addDerivedColumn(workingDerivedCol);
        cancel();
    }

    function deleteColumn(d) {
        gridService.removeColumn(d);
        cancel();
    }

    function saveColumnDefs() {
        gridService.saveColumns()
            .then(() => {
                onSave();
                toasts.success("Report grid columns updated successfully");
                selectedColumn = null;
                activeMode = Modes.VIEW;
                $filters = [];
                activeSummaries.set([]);
            })
            .catch(() => toasts.error("Unable to update report grid"));
    }

    function editColumn(column){
        selectedColumn = column;
        activeMode = Modes.EDIT;
    }

    function removeColumn(column) {
        selectedColumn = column;
        activeMode = Modes.DELETE;
    }

    function cancel() {
        clearWorking();
        selectedColumn = null;
        activeMode = Modes.VIEW;
    }

    function determineEditComponent(kind) {
        switch (kind) {
            case "REPORT_GRID_FIXED_COLUMN_DEFINITION":
                return FixedColumnDetailsEditor;
            case "REPORT_GRID_DERIVED_COLUMN_DEFINITION":
                return DerivedColumnDetailsEditor
            default:
                throw `Cannot determine edit component for column kind: ${kind}`;
        }
    }

    $: canBeAdded = (d) => {
        return !_.some(
            $columnDefs,
            r => sameColumnRef(r, d));
    }

    $: comp = selectedColumn && determineEditComponent(selectedColumn?.kind);

</script>

<div class="help-block small"
     style="margin-bottom: 1em">
        <Icon name="info-circle"/>The columns for this report grid are listed below. You can add, remove or edit the column details.
        Use the arrows to change the order of the columns in the report grid.
</div>

{#if $hasDirtyColumns}
    <NoData type="warning">
        <div>
            The columns for this report grid have been modified, to keep the changes please save this report.
        </div>

        <button style="margin-top: 1em;"
                class="btn btn-success"
                on:click={() => saveColumnDefs()}>
            <Icon name="floppy-o"/>
            Save this report
        </button>
    </NoData>
{/if}

<div class="row">

    <div class="col-sm-6"
         style="padding-left: 0; padding-right: 0">
        <ReportGridColumnSummary {gridId}
                                 selectedColumn={selectedColumn}
                                 onRemove={removeColumn}
                                 onEdit={editColumn}/>
    </div>

    <div class="col-sm-6">
        {#if activeMode === Modes.EDIT}
            <svelte:component this={comp}
                              column={selectedColumn}
                              onCancel={cancel}
                              onRemove={removeColumn}/>
        {:else if activeMode === Modes.DELETE}
            <ColumnRemovalConfirmation column={selectedColumn}
                                       onCancel={cancel}
                                       onRemove={deleteColumn}/>
        {:else if activeMode === Modes.VIEW}
            <div style="padding-bottom: 1em">
                <strong>Add a column</strong> to the report grid, you can construct a grid from a combination of
                entities:
                e.g. viewpoints, assessments, survey question responses, or fields:
                e.g. application kind, survey due date etc.
            </div>
            {#if $gridDefinition}
                <EntitySelector onSelect={onSelect}
                                onDeselect={deleteColumn}
                                selectionFilter={canBeAdded}
                                subjectKind={$gridDefinition.subjectKind}/>
            {/if}
            <div>
                You can also
                <button class="btn btn-skinny"
                        on:click={() => activeMode = Modes.DERIVED}>
                    <strong>add a derived column</strong>
                </button>
                which can be based on fixed columns or other derived columns.
            </div>
        {:else if activeMode === Modes.DERIVED}
            <h4>Add a derived column:</h4>
            <div style="padding-bottom: 1em">
                <strong>Display name</strong>
                <div class="small help-text">The name displayed on the grid. This cannot be changed once saved.</div>
                <input class="form-control"
                       required
                       id="title"
                       placeholder="Display name"
                       bind:value={workingDerivedCol.displayName}>
            </div>
            <div style="padding-bottom: 1em">
                <strong>External Id</strong>
                <div class="small help-text">A name used to reference this column in other derivation scripts.</div>
                <input class="form-control"
                       required
                       id="externalId"
                       placeholder="External Id"
                       bind:value={workingDerivedCol.externalId}>
            </div>
            <div style="padding-bottom: 1em">
                <strong>Description</strong>
                <div class="small help-text">A longer description of this derived column.</div>
                <textarea class="form-control code"
                          id="columnDescription"
                          rows="2"
                          placeholder="Enter description here"
                          bind:value={workingDerivedCol.columnDescription}/>
            </div>
            <div style="padding-bottom: 1em">
                <strong>Derivation Script</strong>
                <div class="small help-text">Calculates the value to be displayed in this column.
                </div>
                <textarea class="form-control code"
                          required
                          id="derivationScript"
                          rows="6"
                          placeholder="Enter script here"
                          bind:value={workingDerivedCol.derivationScript}/>
                <br>
                <Markdown text={derivedColumnHelpText}/>
            </div>
            <span>
                <button class="btn btn-skinny"
                        disabled={workingDerivedCol.displayName == null || workingDerivedCol.derivationScript == null}
                        on:click={() => addDerivedColumn()}>
                    Done
                </button>
                |
                <button class="btn btn-skinny"
                        on:click={() => cancel()}>
                    Cancel
                </button>
            </span>
        {/if}
    </div>
</div>

<style>
    .code {
        font-family: monospace;
    }
</style>