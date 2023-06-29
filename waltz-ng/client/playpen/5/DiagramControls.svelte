<script>

    import {selectedGroup, workingGroup, FlexDirections, DefaultProps} from "./diagram-builder-store";
    import _ from "lodash";

    $workingGroup = Object.assign({}, $selectedGroup);

    function toggleFlexDirection() {
        $workingGroup.props.flexDirection = $workingGroup.props.flexDirection === FlexDirections.ROW
            ? FlexDirections.COLUMN
            : FlexDirections.ROW
    }

    function addItemToGroup() {
        const itemNumber = _.size($workingGroup.items) + 1;
        const newItem = {title: "Item " + itemNumber.toString(), items: [], props: DefaultProps, parentId: $workingGroup.id}
        $workingGroup.items = _.concat($workingGroup.items, newItem);
    }

    function removeItem() {
        $workingGroup.items = _.remove($workingGroup.items, newItem);
    }

    function clearGroup() {
        $workingGroup = $selectedGroup;
    }

</script>

{#if $selectedGroup}
    <div class="diagram-control-panel">
        {#if $workingGroup}
            Selected Group: {$workingGroup.title}
            Item Count: {_.size($workingGroup)}
            <br>
            <button class="btn btn-default"
                    on:click={() => toggleFlexDirection()}>
                Toggle alignment
            </button>
            <button class="btn btn-default"
                    on:click={addItemToGroup}>
                Add item
            </button>
            <button class="btn btn-default"
                    on:click={removeItem}>
                Add item
            </button>
            <button class="btn btn-default"
                    on:click={clearGroup}>
                Reset Group
            </button>
        {/if}
    </div>
{/if}



<style>


    .diagram-control-panel {
        padding: 5em 0;
    }


</style>