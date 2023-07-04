<script>
    import {groups, selectedGroup} from "../diagram-builder-store";
    import _ from "lodash";
    import {ControlModes, FlexDirections, mkGroup} from "../diagram-builder-utils";
    import EditGroupPanel from "./EditGroupPanel.svelte";
    import EditItemsPanel from "./EditItemsPanel.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import {flattenChildren} from "../../../common/hierarchy-utils";
    import GroupDetailsPanel from "./GroupDetailsPanel.svelte";

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


        console.log({childGroups});

        const withoutGroup = _.reject($groups, d => d.parentId === $selectedGroup.id);

        $groups = _.concat(withoutGroup, ...childGroups);
    }

    function addGroup() {
        const groupNumber = _.size($groups) + 1;
        const newGroup = mkGroup("Group " + groupNumber.toString(), groupNumber, $selectedGroup.id, groupNumber)
        $groups = _.concat($groups, newGroup);
    }

    function removeGroup() {
        const children = flattenChildren($selectedGroup);
        const groupsToRemove = _.concat(children, $selectedGroup);
        $groups = _.reject($groups, d => _.includes(_.map(groupsToRemove, d => d.id), d.id));
        $selectedGroup = null;
    }

    function saveGroup(group) {
        console.log("saving", {group});
        const updatedGroup = group.detail;
        _.reject($groups, d => d.id === updatedGroup.id);
        $groups = _.concat($groups, updatedGroup);
        activeMode = ControlModes.VIEW
        $selectedGroup = updatedGroup; // might need to re find this from the hierarchy
    }


    $: console.log({groups: $groups});

</script>


{#if $selectedGroup}
<div class="row">
    <div class="col-md-12">
        {#if activeMode === ControlModes.VIEW}
            <GroupDetailsPanel/>

            <button class="btn btn-default"
                    on:click={() => toggleFlexDirection()}>
                Toggle alignment
            </button>
            <button class="btn btn-default"
                    on:click={() => toggleItemTitleDisplay()}>
                Toggle Item Title Display
            </button>
            <button class="btn btn-default"
                    on:click={() => activeMode = ControlModes.EDIT_GROUP}>
                Edit Group Details
            </button>
            <button class="btn btn-default"
                    on:click={addGroup}>
                Add Child Group
            </button>
            <button class="btn btn-default"
                    disabled={_.isEmpty($selectedGroup.itemKind)}
                    on:click={() => activeMode = ControlModes.EDIT_ITEMS}>
                Edit Items
            </button>
            <button class="btn btn-default"
                    on:click={removeGroup}>
                Remove Group
            </button>

        {:else if activeMode === ControlModes.EDIT_GROUP}
            <EditGroupPanel on:save={saveGroup}
                            on:cancel={() => activeMode = ControlModes.VIEW}/>
        {:else if activeMode === ControlModes.EDIT_ITEMS}
            <EditItemsPanel on:cancel={() => activeMode = ControlModes.VIEW}/>
        {/if}
    </div>
</div>
{/if}
