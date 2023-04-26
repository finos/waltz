<script>
    import DropdownPicker from "./DropdownPicker.svelte";
    import _ from "lodash";
    import {additionalColumnOptions, sameColumnRef} from "../report-grid-utils";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {columnDefs, selectedGrid} from "../report-grid-store";
    import ColumnDefinitionHeader from "./ColumnDefinitionHeader.svelte";
    import {reportGridStore} from "../../../../svelte-stores/report-grid-store";

    export let column;
    export let onCancel = () => console.log("Close");
    export let onRemove = () => console.log("Remove");

    $: columnOptionsCall = reportGridStore.findAdditionalColumnOptionsForKind(column.columnEntityKind);
    $: allowedColumnOptions = _.map($columnOptionsCall?.data, d => additionalColumnOptions[d]) || [additionalColumnOptions.NONE];

    let working = {
        id: column.id,
        displayName: column.displayName,
        externalId: column.externalId,
    }

    $: {
        if (column && column.id !== working.id) {
            setWorkingColumn(column);
        }
    }

    function cancelEdit() {
        onCancel();
    }

    function setWorkingColumn(col) {
        working = {
            id: col.id,
            displayName: col.displayName,
            externalId: col.externalId,
            additionalColumnOptions: col.additionalColumnOptions
        };
    }

    function clearEdit(column) {
        const originalColumn = _.find($selectedGrid.definition.fixedColumnDefinitions, d => sameColumnRef(d, column));
        const columnsWithoutCol = _.reject($columnDefs, d => sameColumnRef(d, column));
        $columnDefs = _.concat(columnsWithoutCol, originalColumn);
        setWorkingColumn(originalColumn);
    }

    function valueChanged(columnDefs, column) {
        const updatedColumn = _.find(columnDefs, d => sameColumnRef(d, column));
        return column.id != null //new columns cannot be reset
            && (updatedColumn?.additionalColumnOptionsChanged
                || updatedColumn?.displayNameChanged
                || updatedColumn?.externalIdChanged);
    }

    function selectColumnOptions(columnOptions, column) {
        const workingColumn = _.find($columnDefs, d => sameColumnRef(d, column));
        const originalColumn = _.find($selectedGrid.definition.fixedColumnDefinitions, d => sameColumnRef(d, column));
        const newColumn = Object.assign(
            {},
            workingColumn,
            {
                additionalColumnOptions: columnOptions?.key,
                additionalColumnOptionsChanged: columnOptions?.key !== originalColumn?.additionalColumnOptions
            })
        const columnsWithoutCol = _.reject($columnDefs, d => sameColumnRef(d, column));
        $columnDefs = _.concat(columnsWithoutCol, newColumn);
    }

    function updateDisplayName(workingDisplayName, column) {
        const workingColumn = _.find($columnDefs, d => sameColumnRef(d, column));
        const originalColumn = _.find($selectedGrid.definition.fixedColumnDefinitions, d => sameColumnRef(d, column));
        const newColumn = Object.assign(
            {},
            workingColumn,
            {
                displayName: workingDisplayName,
                displayNameChanged: workingDisplayName !== originalColumn?.displayName
            })
        const columnsWithoutCol = _.reject($columnDefs, d => sameColumnRef(d, column));
        $columnDefs = _.concat(columnsWithoutCol, newColumn);
    }

    function updateExternalId(workingExternalId, column) {
        const workingColumn = _.find($columnDefs, d => sameColumnRef(d, column));
        const originalColumn = _.find($selectedGrid.definition.fixedColumnDefinitions, d => sameColumnRef(d, column));
        const newColumn = Object.assign(
            {},
            workingColumn,
            {
                externalId: workingExternalId,
                externalIdChanged: workingExternalId !== originalColumn?.externalId
            })
        const columnsWithoutCol = _.reject($columnDefs, d => sameColumnRef(d, column));
        $columnDefs = _.concat(columnsWithoutCol, newColumn);
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
            <div>Additional Column Options</div>
            <div class="small help-text">
                Select any additional column options used to determine the column value
            </div>
        </td>
        <td>
            <DropdownPicker items={allowedColumnOptions}
                            onSelect={(d) => selectColumnOptions(d, column)}
                            defaultMessage="Select additional column options"
                            selectedItem={additionalColumnOptions[column.additionalColumnOptions]}/>
        </td>
    </tr>
    <tr>
        <td>
            <div>Display name</div>
            <div class="small help-text">The name displayed on the grid. By default the entity name is displayed
            </div>
        </td>
        <td>
            <input class="form-control"
                   id="displayName"
                   on:change={() => updateDisplayName(working.displayName, column)}
                   placeholder="Display name"
                   bind:value={working.displayName}>
        </td>
    </tr>
    <tr>
        <td>
            <div>External ID (Recommended)</div>
            <div class="small help-text">An identifier used to reference this column in derivation scripts and filter
                notes
            </div>
        </td>
        <td>
            <input class="form-control"
                   id="externalId"
                   on:change={() => updateExternalId(working.externalId, column)}
                   placeholder="External Id"
                   bind:value={working.externalId}>
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
