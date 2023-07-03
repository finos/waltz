<script>
    import {groups, selectedGroup} from "../diagram-builder-store";
    import _ from "lodash";
    import {ControlModes, FlexDirections, mkGroup} from "../diagram-builder-utils";
    import EditGroupPanel from "./EditGroupPanel.svelte";
    import EditItemsPanel from "./EditItemsPanel.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import {flattenChildren} from "../../../common/hierarchy-utils";

    let activeMode = ControlModes.VIEW;

    function toggleFlexDirection() {
        const group = _.find($groups, d => d.id === $selectedGroup.id);

        group.props.flexDirection = group.props.flexDirection ===  FlexDirections.ROW
            ? FlexDirections.COLUMN
            : FlexDirections.ROW;

        const withoutGroup = _.reject($groups, d => d.id === $selectedGroup.id);
        $groups = _.concat(withoutGroup, group);
    }

    function addGroup() {
        const groupNumber = _.size($groups) + 1;
        const newGroup = mkGroup("Group " + groupNumber.toString(), groupNumber, $selectedGroup.id, groupNumber)
        $groups = _.concat($groups, newGroup);
    }


    function removeGroup() {
        const children = flattenChildren($selectedGroup);
        const groupsToRemove = _.concat(children, $selectedGroup);
        console.log({sg: $selectedGroup, children});
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
            <h4>{$selectedGroup.title}</h4>
            <ul>
                <li>D: {$selectedGroup.id}</li>
                <li>Item Kind: {$selectedGroup.itemKind}</li>
                <li>Properties:</li>
                <li>
                    <ul>
                        <li>Item Height: {$selectedGroup.props.itemHeight}em</li>
                        <li>Item Width: {$selectedGroup.props.itemWidth}em</li>
                        <li>Alignment: {$selectedGroup.props.flexDirection}</li>
                        <li>Groups Per Row: {$selectedGroup.props.groupsPerRow}</li>
                        <li>Groups Per Column: {$selectedGroup.props.groupsPerColumn}</li>
                    </ul>
                </li>
            </ul>

            <button class="btn btn-default"
                    on:click={() => toggleFlexDirection()}>
                Toggle alignment
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
