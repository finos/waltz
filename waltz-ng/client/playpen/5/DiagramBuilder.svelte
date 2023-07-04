<script>

    import DiagramGroup from "./DiagramGroup.svelte";
    import {groupsWithItems, selectedGroup} from "./diagram-builder-store";
    import DiagramControls from "./control-panel/DiagramControls.svelte";
    import {buildHierarchies} from "../../common/hierarchy-utils";
    import DiagramTreeSelector from "./DiagramTreeSelector.svelte";
    import _ from "lodash";
    import DiagramView from "./DiagramView.svelte";
    import GroupDetailsPanel from "./control-panel/GroupDetailsPanel.svelte";
    import Toggle from "../../common/svelte/Toggle.svelte";

    let items = [];

    let editing = false;


    function selectGroup(group) {
        $selectedGroup = group;
    }

    function deselectGroup() {
        $selectedGroup = null;
    }

    $: groupHierarchy  = _.first(buildHierarchies($groupsWithItems)); // take first as starting from root node

</script>


<div>
    <div class="col-sm-6">
        {#if editing}
            <DiagramControls/>
        {:else}
            <GroupDetailsPanel/>
        {/if}
    </div>
    <div class="col-sm-6" style="border-left: 1px solid #ccc">
        <h4>Structure</h4>
        <DiagramTreeSelector groups={$groupsWithItems}
                             onSelect={selectGroup}
                             onDeselect={deselectGroup}/>

        <div style="padding-top: 2em">
            <Toggle labelOn="Editing Diagram"
                    labelOff="Viewing Diagram"
                    state={editing}
                    onToggle={() => editing = !editing}/>
            <div class="small help-block">
                You can toggle between view and edit modes using this control.
            </div>
        </div>
    </div>
</div>

<div class="col-sm-12"
     style="margin-top: 2em">
    {#if editing}
        <DiagramGroup group={groupHierarchy}>
        </DiagramGroup>
    {:else}
        <DiagramView group={groupHierarchy}>
        </DiagramView>
    {/if}
</div>