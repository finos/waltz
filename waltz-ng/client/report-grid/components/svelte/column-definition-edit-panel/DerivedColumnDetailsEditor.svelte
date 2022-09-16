<script>
    import DropdownPicker from "./DropdownPicker.svelte";
    import _ from "lodash";
    import {columnUsageKind, ratingRollupRule, sameColumnRef} from "../report-grid-utils";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {columnDefs, selectedGrid} from "../report-grid-store";
    import {sameRef} from "../../../../common/entity-utils";
    import ColumnDefinitionHeader from "./ColumnDefinitionHeader.svelte";

    export let column;
    export let onCancel = () => console.log("Close");
    export let onRemove = () => console.log("Remove");

    let workingDisplayName = column.displayName;

    function cancelEdit(){
        onCancel();
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
            <div class="small help-text">The name displayed on the grid.
            </div>
        </td>
        <td>
            <input class="form-control"
                   required
                   id="title"
                   on:change={() => updateDisplayName(workingDisplayName, column)}
                   placeholder="Display name"
                   bind:value={workingDisplayName}>
        </td>
    </tr>
    </tbody>
</table>

<button class="btn btn-skinny"
        on:click={cancelEdit}>
    <Icon name="times"/>Close
</button>
|
<button class="btn btn-skinny"
        on:click={() => onRemove(column)}>
    <Icon name="trash"/>Delete
</button>
