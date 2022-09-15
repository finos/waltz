<script>
    import EntitySelector from "./EntitySelector.svelte";
    import _ from "lodash";
    import ReportGridColumnSummary from "./ReportGridColumnSummary.svelte";
    import {columnUsageKind, determineDefaultRollupRule, sameColumnRef} from "../report-grid-utils";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {reportGridStore} from "../../../../svelte-stores/report-grid-store";
    import toasts from "../../../../svelte-stores/toast-store";
    import ColumnDetailsEditor from "./ColumnDetailsEditor.svelte";
    import NoData from "../../../../common/svelte/NoData.svelte";
    import {
        columnDefs,
        hasChanged,
        selectedColumn,
        lastMovedColumn,
        selectedGrid,
        allColumnDefs
    } from "../report-grid-store";
    import ColumnRemovalConfirmation from "./ColumnRemovalConfirmation.svelte";
    import {entity} from "../../../../common/services/enums/entity";


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
                { position: _.indexOf(newList, d) }));
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

        const columnDefs = _.map(
            columns,
            d => ({
                columnEntityKind: d.columnEntityKind,
                columnEntityId: d.columnEntityId,
                position: d.position,
                ratingRollupRule: d.ratingRollupRule,
                entityFieldReference: d.entityFieldReference,
                displayName: d.displayName,
                columnQualifierKind: d.columnQualifierKind,
                columnQualifierId: d.columnQualifierId
            }));

        return reportGridStore
            .updateColumnDefinitions(gridId, {fixedColumnDefinitions: columnDefs})
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
            r => sameColumnRef(r, d));

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
        $selectedColumn = null;
        activeMode = Modes.VIEW
    }

    $: console.log({allColDefs: $allColumnDefs})

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
    <div class="col-sm-4">
        <div style="padding-bottom: 1em">
            <strong>Add a column</strong> to the report grid, you can construct a grid from a combination of entities:
            e.g. viewpoints, assessments, survey question responses, or fields:
            e.g. application kind, survey due date etc.
        </div>
        <EntitySelector onSelect={onSelect}
                        onDeselect={deleteEntity}
                        selectionFilter={canBeAdded}
                        subjectKind={$selectedGrid?.definition.subjectKind}/>
    </div>

    <div class="col-sm-4"
         style="padding-left: 0; padding-right: 0">
        <ReportGridColumnSummary {gridId}
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