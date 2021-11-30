<script>

    import NoData from "../../common/svelte/NoData.svelte";
    import Icon from "../../common/svelte/Icon.svelte";
    import DropdownPicker from "./DropdownPicker.svelte";
    import {columnUsageKind, ratingRollupRule} from "./report-grid-utils";
    import _ from "lodash"
    import {entity} from "../../common/services/enums/entity";
    import {move} from "../../common/list-utils";
    import {reportGridStore} from "../../svelte-stores/report-grid-store";
    import toasts from "../../svelte-stores/toast-store";

    export let onRemove = () => console.log("Removing entity")
    export let columnDefs = [];
    export let gridId;

    let originalColumnDefs = columnDefs;

    let defaultUsageKind = columnUsageKind.NONE
    let defaultRollupKind = ratingRollupRule.NONE

    $: moveUp = (entity) => {
        const reorderedList = move(columnDefs, _.indexOf(columnDefs, entity), -1);
        columnDefs = _.map(reorderedList, d => Object.assign({}, d, {position: _.indexOf(reorderedList, d)}));
    }

    $: moveDown = (entity) => {
        const reorderedList = move(columnDefs, _.indexOf(columnDefs, entity), 1);
        columnDefs = _.map(reorderedList, d => Object.assign({}, d, {position: _.indexOf(reorderedList, d)}))
    }

    function selectSummary(usageKind, column) {
        column.usageKind = usageKind?.key;
    }

    function selectRollupKind(rollupKind, column) {
        column.ratingRollupRule = rollupKind?.key;
    }

    function getIcon(entityKind) {
        return _.get(entity[entityKind], 'icon', "fw");
    }

    function saveColumnDefs() {
        return reportGridStore
            .updateColumnDefinitions(gridId, {columnDefinitions: columnDefs})
            .then(() => toasts.success("Report grid columns updated successfully"))
            .catch(() => toasts.error("Unable to update report grid"));
    }

    function cancel(){
        columnDefs = originalColumnDefs;
    }

    $: summaryItems = _.values(columnUsageKind);
    $: rollupKinds = _.values(ratingRollupRule);

</script>

<div class="row">
    <div class="col-sm-12">
        <table class="table table-condensed table-striped">
            <colgroup>
                <col width="20%">
                <col width="20%">
                <col width="20%">
                <col width="20%">
                <col width="20%">
            </colgroup>
            <thead>
                <tr>
                    <th>Entity</th>
                    <th>Position</th>
                    <th>Summary</th>
                    <th>Rollup Kind</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                {#each columnDefs as column}
                    <tr>
                        <td>
                            <Icon name={getIcon(column.columnEntityReference.kind)}/>{column.columnEntityReference.name || column.columnEntityReference.questionText}
                        </td>
                        <td>
                            <span style="text-align: center">
                                <button class="btn btn-skinny small"
                                        on:click={() => moveUp(column)}>
                                    <Icon name="arrow-up"/>
                                </button>
                                <button class="btn btn-skinny small"
                                        on:click={() => moveDown(column)}>
                                    <Icon name="arrow-down"/>
                                </button>
                            </span>
                        </td>
                        <td>
                            <DropdownPicker items={summaryItems}
                                            onSelect={(d) => selectSummary(d, column)}
                                            defaultMessage="Select a summary kind"
                                            selectedItem={columnUsageKind[column.usageKind]}/>
                        </td>
                        <td>
                            {#if column.columnEntityReference.kind === 'MEASURABLE'}
                                <DropdownPicker items={rollupKinds}
                                                onSelect={(d) => selectRollupKind(d, column)}
                                                defaultMessage="Select a rollup kind"
                                                selectedItem={ratingRollupRule[column.ratingRollupRule]}/>
                            {:else}
                                <span>{ratingRollupRule.NONE.name}</span>
                            {/if}
                        </td>
                        <td>
                            <button class="btn btn-skinny small"
                                    on:click={() => onRemove(column)}>
                                <Icon name="trash"/>Remove
                            </button>
                        </td>
                    </tr>
                {:else}
                    <tr>
                        <td colspan="5">
                            <NoData>You have no entities selected</NoData>
                        </td>
                    </tr>
                {/each}
            </tbody>
        </table>
    </div>
</div>

<div class="row">
    <div class="col-sm-12">
        <span>
            <button class="btn btn-skinny"
                    on:click={() => saveColumnDefs()}>
                <Icon name="floppy-o"/>Save column updates
            </button>
            {#if columnDefs !== originalColumnDefs}
            |
            <button class="btn btn-skinny"
                    on:click={() => cancel()}>
                <Icon name="ban"/>Cancel
            </button>
            {/if}
        </span>
    </div>
</div>