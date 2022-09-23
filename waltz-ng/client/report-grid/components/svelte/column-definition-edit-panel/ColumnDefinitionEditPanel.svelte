<script>
    import EntitySelector from "./EntitySelector.svelte";
    import _ from "lodash";
    import ReportGridColumnSummary from "./ReportGridColumnSummary.svelte";
    import {determineDefaultRollupRule, sameColumnRef} from "../report-grid-utils";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {reportGridStore} from "../../../../svelte-stores/report-grid-store";
    import toasts from "../../../../svelte-stores/toast-store";
    import FixedColumnDetailsEditor from "./FixedColumnDetailsEditor.svelte";
    import DerivedColumnDetailsEditor from "./DerivedColumnDetailsEditor.svelte";
    import NoData from "../../../../common/svelte/NoData.svelte";
    import {columnDefs, hasChanged, lastMovedColumn, selectedColumn, selectedGrid,} from "../report-grid-store";
    import ColumnRemovalConfirmation from "./ColumnRemovalConfirmation.svelte";
    import {entity} from "../../../../common/services/enums/entity";


    export let gridId;
    export let onSave = () => console.log("Saved report grid");

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT",
        DELETE: "DELETE",
        DERIVED: "DERIVED"
    }

    let activeMode = Modes.VIEW;

    let canBeAdded;

    let workingDerivedCol = {
        displayName: null,
        derivationScript: null,
        externalId: null,
        columnDescription: null
    }

    function clearWorking() {
        workingDerivedCol = {
            displayName: null,
            derivationScript: null,
            externalId: null,
            columnDescription: null
        }
    }

    function onSelect(d) {

        //Only fixed columns are chosen through picker
        const column = {
            kind: "REPORT_GRID_FIXED_COLUMN_DEFINITION",
            columnEntityId: d.columnEntityId,
            columnEntityKind: d.columnEntityKind,
            entityFieldReference: d.entityFieldReference,
            columnQualifierKind: d.columnQualifierKind,
            columnQualifierId: d.columnQualifierId,
            columnName: d.columnName,
            columnDescription: d.columnDescription,
            ratingRollupRule: determineDefaultRollupRule(d).key,
            displayName: d.displayName,
            position: 0
        };

        if (_.some($columnDefs, c => sameColumnRef(column, c))) {
            return;
        }

        const newList = _.concat(
            $columnDefs,
            column);

        $columnDefs = _.map(
            newList,
            d => Object.assign(
                {},
                d,
                {position: _.indexOf(newList, d)}));
    }


    function addDerivedColumn() {

        const column = {
            kind: "REPORT_GRID_DERIVED_COLUMN_DEFINITION",
            columnEntityKind: "REPORT_GRID_DERIVED_COLUMN_DEFINITION",
            displayName: workingDerivedCol.displayName,
            columnDescription: workingDerivedCol.columnDescription,
            externalId: workingDerivedCol.externalId,
            derivationScript: workingDerivedCol.derivationScript,
            position: 0
        };

        if (_.some($columnDefs, c => sameColumnRef(column, c))) {
            return;
        }

        const newList = _.concat(
            $columnDefs,
            column);

        $columnDefs = _.map(
            newList,
            d => Object.assign(
                {},
                d,
                {position: _.indexOf(newList, d)}));

        cancel();
    }

    function deleteColumn(d) {
        $columnDefs = _.reject(
            $columnDefs,
            r => sameColumnRef(
                r,
                d));
        cancel();
    }

    function deleteEntity(d) {
        $columnDefs = _.reject(
            $columnDefs,
            r => sameColumnRef(r, d));
        cancel();
    }

    function saveColumnDefs(columns) {

        let fixedColDefs = _.filter(columns, d => d.kind === "REPORT_GRID_FIXED_COLUMN_DEFINITION");
        let derivedColDefs = _.filter(columns, d => d.kind === "REPORT_GRID_DERIVED_COLUMN_DEFINITION");

        const fixedColumnDefinitions = _.map(
            fixedColDefs,
            d => ({
                columnEntityKind: d.columnEntityKind,
                columnEntityId: d.columnEntityId,
                position: d.position,
                ratingRollupRule: d.ratingRollupRule,
                entityFieldReference: d.entityFieldReference,
                displayName: d.displayName,
                columnQualifierKind: d.columnQualifierKind,
                columnQualifierId: d.columnQualifierId,
                externalId: d.externalId
            }));

        const derivedColumnDefinitions = _.map(
            derivedColDefs,
            d => ({
                position: d.position,
                displayName: d.displayName,
                derivationScript: d.derivationScript,
                externalId: d.externalId
            }));

        return reportGridStore
            .updateColumnDefinitions(gridId, {fixedColumnDefinitions, derivedColumnDefinitions})
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
        const notAlreadyAdded = !_.some(
            $columnDefs,
            r => sameColumnRef(r, Object.assign({}, {
                kind: "REPORT_GRID_FIXED_COLUMN_DEFINITION",
                columnEntityId: d.id,
                columnEntityKind: d.kind
            })));

        switch (d.kind) {
            case entity.ASSESSMENT_DEFINITION.key:
                const assessmentAllowableForThisGrid = _.get(d, ["entityKind"]) === $selectedGrid?.definition?.subjectKind;
                return notAlreadyAdded && assessmentAllowableForThisGrid;
            case entity.SURVEY_TEMPLATE.key:
                return _.get(d, ["targetEntityKind"]) === $selectedGrid?.definition?.subjectKind;
            default:
                return notAlreadyAdded;
        }
    }

    function editColumn(column){
        $selectedColumn = column;
        activeMode = Modes.EDIT;
    }

    function removeColumn(column) {
        $selectedColumn = column;
        activeMode = Modes.DELETE;
    }

    function cancel() {
        clearWorking();
        $selectedColumn = null;
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

    $: comp = $selectedColumn && determineEditComponent($selectedColumn?.kind);

</script>

<div class="help-block small"
     style="margin-bottom: 1em">
        <Icon name="info-circle"/>The columns for this report grid are listed below. You can add, remove or edit the column details.
        Use the arrows to change the order of the columns in the report grid.
</div>

{#if $hasChanged}
    <NoData type="warning">
        <div>
            The columns for this report grid have been modified, to keep the changes please save this report.
        </div>

        <button style="margin-top: 1em;"
                class="btn btn-success"
                on:click={() => saveColumnDefs($columnDefs)}>
            <Icon name="floppy-o"/>Save this report
        </button>
    </NoData>
{/if}

<div class="row">

    <div class="col-sm-6"
         style="padding-left: 0; padding-right: 0">
        <ReportGridColumnSummary {gridId}
                                 onRemove={removeColumn}
                                 onEdit={editColumn}/>
    </div>

    <div class="col-sm-6">
        {#if activeMode === Modes.EDIT}
            <svelte:component this={comp}
                              column={$selectedColumn}
                              onCancel={cancel}
                              onRemove={removeColumn}/>
        {:else if activeMode === Modes.DELETE}
            <ColumnRemovalConfirmation column={$selectedColumn}
                                       onCancel={cancel}
                                       onRemove={deleteColumn}/>
        {:else if activeMode === Modes.VIEW}
            <div style="padding-bottom: 1em">
                <strong>Add a column</strong> to the report grid, you can construct a grid from a combination of
                entities:
                e.g. viewpoints, assessments, survey question responses, or fields:
                e.g. application kind, survey due date etc.
            </div>
            <EntitySelector onSelect={onSelect}
                            onDeselect={deleteEntity}
                            selectionFilter={canBeAdded}
                            subjectKind={$selectedGrid?.definition.subjectKind}/>
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
                <div class="small help-text">The name displayed on the grid.</div>
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
                <div class="small help-text">A description of this column.</div>
                <input class="form-control"
                       required
                       id="description"
                       placeholder="Description"
                       bind:value={workingDerivedCol.columnDescription}>
            </div>
            <div style="padding-bottom: 1em">
                <strong>Derivation Script</strong>
                <div class="small help-text">Calculates the value to be displayed in this column.
                </div>
                <textarea class="form-control"
                          required
                          id="derivationScript"
                          rows="6"
                          placeholder="Enter script here"
                          bind:value={workingDerivedCol.derivationScript}/>
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