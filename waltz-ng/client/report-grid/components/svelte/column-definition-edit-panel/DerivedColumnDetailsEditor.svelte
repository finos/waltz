<script>
    import _ from "lodash";
    import {sameColumnRef} from "../report-grid-utils";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {columnDefs, selectedGrid} from "../report-grid-store";
    import ColumnDefinitionHeader from "./ColumnDefinitionHeader.svelte"
    import Markdown from "../../../../common/svelte/Markdown.svelte";
    import {derivedColumnHelpText} from "./column-definition-utils";

    export let column;
    export let onCancel = () => console.log("Close");
    export let onRemove = () => console.log("Remove");


    let working = {
        id: column.id,
        displayName: column.displayName,
        externalId: column.externalId,
        derivationScript: column.derivationScript
    }

    function cancelEdit() {
        onCancel();
    }

    function setWorkingColumn(col) {
        working = {
            id: col.id,
            displayName: col.displayName,
            externalId: col.externalId,
            derivationScript: col.derivationScript
        };
    }

    function clearEdit(column) {
        const originalColumn = _.find($selectedGrid.definition.derivedColumnDefinitions, d => sameColumnRef(d, column));
        const columnsWithoutCol = _.reject($columnDefs, d => sameColumnRef(d, column));
        $columnDefs = _.concat(columnsWithoutCol, originalColumn);
        setWorkingColumn(originalColumn);
    }

    function valueChanged(columnDefs, column) {
        const updatedColumn = _.find(columnDefs, d => sameColumnRef(d, column));
        return column.id != null //new columns cannot be reset
            && (updatedColumn.derivationScriptChanged
                || updatedColumn.displayNameChanged
                || updatedColumn.externalIdChanged);
    }

    function updateDisplayName(workingDisplayName, column) {
        const originalColumn = _.find($selectedGrid.definition.derivedColumnDefinitions, d => sameColumnRef(d, column));
        const newColumn = Object.assign(
            {},
            column,
            {
                displayName: workingDisplayName,
                displayNameChanged: workingDisplayName !== originalColumn?.displayName
            })
        const columnsWithoutCol = _.reject($columnDefs, d => sameColumnRef(d, column));
        $columnDefs = _.concat(columnsWithoutCol, newColumn);
    }

    function updateExternalId(workingExternalId, column) {
        const originalColumn = _.find($selectedGrid.definition.derivedColumnDefinitions, d => sameColumnRef(d, column));
        const newColumn = Object.assign(
            {},
            column,
            {
                externalId: workingExternalId,
                externalIdChanged: workingExternalId !== originalColumn?.externalId
            })
        const columnsWithoutCol = _.reject($columnDefs, d => sameColumnRef(d, column));
        $columnDefs = _.concat(columnsWithoutCol, newColumn);
    }

    function updateDerivationScript(workingScript, column) {
        const originalColumn = _.find($selectedGrid.definition.derivedColumnDefinitions, d => sameColumnRef(d, column));
        const newColumn = Object.assign(
            {},
            column,
            {
                derivationScript: workingScript,
                derivationScriptChanged: workingScript !== originalColumn?.derivationScript
            })
        const columnsWithoutCol = _.reject($columnDefs, d => sameColumnRef(d, column));
        $columnDefs = _.concat(columnsWithoutCol, newColumn);
    }

    $: {
        if (column && column.id !== working.id) {
            setWorkingColumn(column);
        }
    }

</script>

<h4>
    <ColumnDefinitionHeader {column}/>
</h4>

<table class="table table-condensed small">
    <colgroup>
        <col width="50%">
        <col width="50%">
    </colgroup>
    <tbody>
    <tr>
        <td>
            <div>Display name</div>
            <div class="small help-text">The name displayed on the grid</div>
        </td>
        <td>
            <input class="form-control"
                   required
                   id="displayName"
                   on:change={() => updateDisplayName(working.displayName, column)}
                   placeholder="Display name"
                   bind:value={working.displayName}>
        </td>
    </tr>
    <tr>
        <td>
            <div>External ID <span class="text-danger">*</span></div>
            <div class="small help-text">An identifier used to reference this column in other derivation scripts and
                fitler notes. This is mandatory for derived columns.
            </div>
        </td>
        <td>
            <input class="form-control"
                   required
                   id="externalId"
                   placeholder="External Id"
                   on:change={() => updateExternalId(working.externalId, column)}
                   bind:value={working.externalId}>
        </td>
    </tr>

    <tr>
        <td>
            <div>Derivation Script</div>
            <div class="small help-text">Calculates the value to be displayed in this column</div>
        </td>
        <td>
            <textarea class="form-control code"
                      required
                      id="derivationScript"
                      rows="6"
                      on:change={() => updateDerivationScript(working.derivationScript, column)}
                      placeholder="Enter script here"
                      bind:value={working.derivationScript}/>
            <br>
            <Markdown text={derivedColumnHelpText}/>
        </td>
    </tr>
    </tbody>
</table>

<button class="btn btn-skinny"
        on:click={cancelEdit}>
    <Icon name="times"/>
    Close
</button>
|
<button class="btn btn-skinny"
        disabled={!valueChanged($columnDefs, column)}
        on:click={() => clearEdit(column)}>
    <Icon name="ban"/>
    Clear
</button>
|
<button class="btn btn-skinny"
        on:click={() => onRemove(column)}>
    <Icon name="trash"/>
    Delete
</button>


<style>
    .code {
        font-family: monospace;
    }
</style>