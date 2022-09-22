<script>
    import DropdownPicker from "./DropdownPicker.svelte";
    import _ from "lodash";
    import {columnUsageKind, ratingRollupRule, sameColumnRef} from "../report-grid-utils";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {columnDefs, selectedGrid} from "../report-grid-store";
    import ColumnDefinitionHeader from "./ColumnDefinitionHeader.svelte";

    export let column;
    export let onCancel = () => console.log("Close");
    export let onRemove = () => console.log("Remove");

    let working = {
        id: column.id,
        displayName: column.displayName,
        externalId: column.externalId,
    }

    $: {
        if (column && column.id !== working.id) {
            working = {
                id: column.id,
                displayName: column.displayName,
                externalId: column.externalId,
            }
        }
    }

    function cancelEdit() {
        onCancel();
    }

    function selectRollupKind(rollupKind, column) {
        const originalColumn = _.find($selectedGrid.definition.fixedColumnDefinitions, d => sameColumnRef(d, column));
        const newColumn = Object.assign(
            {},
            column,
            {
                ratingRollupRule: rollupKind?.key,
                ratingRollupRuleChanged: rollupKind?.key !== originalColumn?.ratingRollupRule
            })
        const columnsWithoutCol = _.reject($columnDefs, d => sameColumnRef(d, column));
        $columnDefs = _.concat(columnsWithoutCol, newColumn);
    }

    function updateDisplayName(workingDisplayName, column) {
        const originalColumn = _.find($selectedGrid.definition.fixedColumnDefinitions, d => sameColumnRef(d, column));
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
        const originalColumn = _.find($selectedGrid.definition.fixedColumnDefinitions, d => sameColumnRef(d, column));
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

    $: rollupKinds = _.values(ratingRollupRule);

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
            <div>Rating rollup rule</div>
            <div class="small help-text">
                Describes the rating value to be displayed when aggregating for a hierarchy.
            </div>
        </td>
        <td>
            {#if column?.columnEntityKind === 'MEASURABLE'}
                <DropdownPicker items={rollupKinds}
                                onSelect={(d) => selectRollupKind(d, column)}
                                defaultMessage="Select rollup kind"
                                selectedItem={ratingRollupRule[column.ratingRollupRule]}/>
            {:else}
                <span>{ratingRollupRule[column.ratingRollupRule].name}</span>
            {/if}
        </td>
    </tr>
    <tr>
        <td>
            <div>Display name</div>
            <div class="small help-text">The name displayed on the grid. By default the entity name is displayed.
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
            <div>External ID</div>
            <div class="small help-text">An identifier used to reference this column in derivation scripts</div>
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
        on:click={() => onRemove(column)}>
    <Icon name="trash"/>
    Delete
</button>
