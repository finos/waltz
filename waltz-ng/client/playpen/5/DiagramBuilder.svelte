<script>

    import DiagramGroup from "./DiagramGroup.svelte";
    import {groups, selectedGroup, workingGroup, groupsWithItems} from "./diagram-builder-store";
    import DiagramControls from "./control-panel/DiagramControls.svelte";
    import {buildHierarchies} from "../../common/hierarchy-utils";
    import DiagramTreeSelector from "./DiagramTreeSelector.svelte";
    import _ from "lodash";

    let items = [];

    function selectGroup(group) {
        $selectedGroup = group;
    }

    function deselectGroup() {
        $selectedGroup = null;
    }

    $: groupHierarchy  = _.first(buildHierarchies($groupsWithItems)); // take first as starting from root node

</script>


<h3>Diagram Builder</h3>
<div>
    <div class="col-sm-6">
        <DiagramControls/>
    </div>
    <div class="col-sm-6" style="border-left: 1px solid #ccc">
        <h4>Structure</h4>
        <DiagramTreeSelector groups={$groupsWithItems}
                             onSelect={selectGroup}
                             onDeselect={deselectGroup}/>
    </div>
</div>

<div class="col-sm-12" style="margin-top: 2em">
    <DiagramGroup group={groupHierarchy}>
    </DiagramGroup>
</div>