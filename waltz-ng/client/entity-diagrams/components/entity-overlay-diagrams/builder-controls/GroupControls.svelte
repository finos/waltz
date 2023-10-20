<script>
    import {diagramService} from "../entity-diagram-store";
    import _ from "lodash";
    import {FlexDirections, mkGroup} from "../entity-diagram-utils";
    import EditGroupPanel from "./EditGroupPanel.svelte";
    import EditItemsPanel from "./EditItemsPanel.svelte";
    import GroupDetailsPanel from "./GroupDetailsPanel.svelte";
    import toasts from "../../../../svelte-stores/toast-store";
    import {generateUUID} from "../../../../system/svelte/nav-aid-builder/custom/builderStore";
    import AddItemsPanel from "./AddItemsPanel.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";

    const ControlModes = {
        VIEW: "VIEW",
        EDIT_GROUP: "EDIT_GROUP",
        EDIT_ITEMS: "EDIT_ITEMS",
        ADD_ITEMS: "ADD_ITEMS",
    }

    let activeMode = ControlModes.VIEW;

    const {selectedGroup, groups, addGroup, removeGroup, updateGroup, updateChildren} = diagramService;

    function toggleFlexDirection() {
        const group = _.find($groups, d => d.id === $selectedGroup.id);

        const updatedProps = Object.assign(
            {},
            group.props,
            {
                flexDirection: group.props.flexDirection === FlexDirections.ROW
                    ? FlexDirections.COLUMN
                    : FlexDirections.ROW
            });

        const updatedGroup = Object.assign({}, group, {props: updatedProps});
        updateGroup(updatedGroup);
    }

    function toggleItemTitleDisplay() {
        const childGroups = _
            .chain($groups)
            .filter(d => d.parentId === $selectedGroup.id)
            .map(d => {
                const updatedProps = Object.assign({}, d.props, {showTitle: !d.props.showTitle});

                return Object.assign({}, d, {props: updatedProps});
            })
            .value();
        updateChildren($selectedGroup.id, childGroups);
    }

    function toggleItemBorderDisplay() {
        const childGroups = _
            .chain($groups)
            .filter(d => d.parentId === $selectedGroup.id)
            .map(d => {
                const updatedProps = Object.assign({}, d.props, {showBorder: !d.props.showBorder});

                return Object.assign({}, d, {props: updatedProps});
            })
            .value();
        updateChildren($selectedGroup.id, childGroups);
    }

    function remove() {
        removeGroup($selectedGroup);
    }

    function saveGroup(group) {
        updateGroup(group.detail)
        activeMode = ControlModes.VIEW;
        toasts.success("Saved group info");
    }

</script>


{#if $selectedGroup}
    <div class="row">
        <div class="col-md-12">
            {#if activeMode === ControlModes.VIEW}

                <GroupDetailsPanel/>

                <div class="help-block">
                    <Icon name="info-circle"/> Use the controls below to update the group details, add items to appear within the selected group,
                    or update the details in bulk for the children of the selected group.
                </div>

                <div class="controls">
                    <button class="btn btn-default"
                            on:click={() => activeMode = ControlModes.EDIT_GROUP}>
                        Edit Group Details
                    </button>
                    <button class="btn btn-default"
                            on:click={() => activeMode = ControlModes.ADD_ITEMS}>
                        Add Item
                    </button>
                    <button class="btn btn-default"
                            on:click={() => activeMode = ControlModes.EDIT_ITEMS}>
                        Edit Item Details
                    </button>
                    <button class="btn btn-default"
                            on:click={() => toggleFlexDirection()}>
                        Toggle Alignment
                    </button>
                    <button class="btn btn-default"
                            disabled={_.isNil($selectedGroup.parentId)}
                            on:click={remove}>
                        Remove Group
                    </button>
                </div>

            {:else if activeMode === ControlModes.EDIT_GROUP}
                <EditGroupPanel on:save={saveGroup}
                                on:cancel={() => activeMode = ControlModes.VIEW}/>
            {:else if activeMode === ControlModes.ADD_ITEMS}
                <AddItemsPanel on:cancel={() => activeMode = ControlModes.VIEW}/>
            {:else if activeMode === ControlModes.EDIT_ITEMS}
                <EditItemsPanel on:cancel={() => activeMode = ControlModes.VIEW}/>
            {/if}
        </div>
    </div>
{:else}
    <div class="help-block">Select an item or group from the diagram or tree to modify it</div>
{/if}


<style>

    .controls {
        display: flex;
        flex-wrap: wrap;
        align-content: flex-start;
        align-items: flex-start;
        gap: 0.5em;
    }

</style>