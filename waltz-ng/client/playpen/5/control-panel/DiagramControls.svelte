<script>
    import {groups, selectedGroup} from "../diagram-builder-store";
    import _ from "lodash";
    import {FlexDirections, mkGroup} from "../diagram-builder-utils";
    import EditGroupPanel from "./EditGroupPanel.svelte";
    import EditItemsPanel from "./EditItemsPanel.svelte";
    import {flattenChildren} from "../../../common/hierarchy-utils";
    import GroupDetailsPanel from "./GroupDetailsPanel.svelte";
    import toasts from "../../../svelte-stores/toast-store";

    const ControlModes = {
        VIEW: "VIEW",
        EDIT_GROUP: "EDIT_GROUP",
        EDIT_ITEMS: "EDIT_ITEMS",
    }

    let activeMode = ControlModes.VIEW;

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

        const withoutGroup = _.reject($groups, d => d.id === $selectedGroup.id);
        $groups = _.concat(withoutGroup, updatedGroup);
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

        const withoutGroup = _.reject($groups, d => d.parentId === $selectedGroup.id);

        $groups = _.concat(withoutGroup, ...childGroups);
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

        const withoutGroup = _.reject($groups, d => d.parentId === $selectedGroup.id);

        $groups = _.concat(withoutGroup, ...childGroups);
    }

    function addGroup() {
        const groupNumber = _.size($groups) + 1;
        const newGroup = mkGroup("Group " + groupNumber.toString(), groupNumber, $selectedGroup.id, groupNumber, $selectedGroup.props)
        $groups = _.concat($groups, newGroup);
    }

    function removeGroup() {
        const children = flattenChildren($selectedGroup);
        const groupsToRemove = _.concat(children, $selectedGroup);
        $groups = _.reject($groups, d => _.includes(_.map(groupsToRemove, d => d.id), d.id));
        $selectedGroup = null;
    }

    function saveGroup(group) {
        const updatedGroup = group.detail;
        const withoutGroup = _.reject($groups, d => d.id === updatedGroup.id);
        $groups = _.concat(withoutGroup, updatedGroup);
        activeMode = ControlModes.VIEW;
        $selectedGroup = _.find($groups, d => d.id === $selectedGroup.id); // might need to re find this from the hierarchy
        toasts.success("Saved group info");
    }

    $: console.log({groups: $groups});

</script>


{#if $selectedGroup}
    <div class="row">
        <div class="col-md-12">
            {#if activeMode === ControlModes.VIEW}
                <GroupDetailsPanel/>

                <div class="controls">
                    <button class="btn btn-default"
                            on:click={() => activeMode = ControlModes.EDIT_GROUP}>
                        Edit Group Details
                    </button>
                    <button class="btn btn-default"
                            on:click={addGroup}>
                        Add Child Group
                    </button>
                    <button class="btn btn-default"
                            on:click={() => activeMode = ControlModes.EDIT_ITEMS}>
                        Edit Items
                    </button>
                    <button class="btn btn-default"
                            on:click={() => toggleFlexDirection()}>
                        Toggle Alignment
                    </button>
                    <button class="btn btn-default"
                            on:click={() => toggleItemTitleDisplay()}>
                        Toggle Item Title Display
                    </button>
                    <button class="btn btn-default"
                            on:click={() => toggleItemBorderDisplay()}>
                        Toggle Item Border Display
                    </button>
                    <button class="btn btn-default"
                            disabled={_.isNil($selectedGroup.parentId)}
                            on:click={removeGroup}>
                        Remove Group
                    </button>
                </div>

            {:else if activeMode === ControlModes.EDIT_GROUP}
                <EditGroupPanel on:save={saveGroup}
                                on:cancel={() => activeMode = ControlModes.VIEW}/>
            {:else if activeMode === ControlModes.EDIT_ITEMS}
                <EditItemsPanel on:cancel={() => activeMode = ControlModes.VIEW}/>
            {/if}
        </div>
    </div>
{:else}
    <div class="help-block">Select an item or group from the diagram or tree to get modify it</div>
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