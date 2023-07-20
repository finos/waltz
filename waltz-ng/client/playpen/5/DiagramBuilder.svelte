<script>

    import DiagramGroup from "./DiagramGroup.svelte";
    import {groupsWithItems, initialGroup, selectedGroup} from "./diagram-builder-store";
    import DiagramControls from "./control-panel/DiagramControls.svelte";
    import {buildHierarchies} from "../../common/hierarchy-utils";
    import DiagramTreeSelector from "./DiagramTreeSelector.svelte";
    import _ from "lodash";
    import DiagramView from "./DiagramView.svelte";
    import GroupDetailsPanel from "./control-panel/GroupDetailsPanel.svelte";
    import Toggle from "../../common/svelte/Toggle.svelte";
    import {prettyHTML} from "../../system/svelte/nav-aid-builder/nav-aid-utils";
    import {groups} from "./diagram-builder-store";
    import {mkGroup} from "./diagram-builder-utils";
    import toasts from "../../svelte-stores/toast-store";

    let items = [];

    let html = "";
    let data = "";
    let vizElem;

    let editing = true;
    let dataInput = false;
    let workingData = "";

    function selectGroup(group) {
        $selectedGroup = group;
    }

    function deselectGroup() {
        $selectedGroup = null;
    }

    $: groupHierarchy = buildHierarchies($groupsWithItems);

    //  think if more than one root node need to force to single parent.
    $: diagram  = _.first(buildHierarchies($groupsWithItems)); // take first as starting from root node


    $: {
        if (vizElem && $groups && !editing) {
            // using a timeout to give innerHTML chance to be updated
            // before we copy it
            setTimeout(() => {
                html = prettyHTML(vizElem.innerHTML);
                data = JSON.stringify($groups, "", 2);
            });
        }
    }

    function saveData() {
        const gs = JSON.parse(workingData);

        const providedGroups = _.map(gs, d => mkGroup(d.title, d.id, d.parentId, d.position, d.props, d.data));
        const roots = buildHierarchies(providedGroups);

        if (_.size(roots) === 1 ) {
            $groups = providedGroups;
            toasts.success("Diagram populated from data");
        } else {
            const initialGroup = mkGroup("Diagram Title", _.size(providedGroups) + 1, null);
            const topLevel = _.map(roots, d => d.id);
            const childGroups = _.map(
                providedGroups,
                d => _.includes(topLevel, d.id)
                    ? Object.assign({}, d, {parentId: initialGroup.id})
                    : d);
            $groups = _.concat(initialGroup, childGroups);
            toasts.warning("There was not a single root to this diagram so a placeholder has been added");
        }
        dataInput = false;
        workingData = "";
    }


    function editData() {
        workingData = JSON.stringify($groups, "", 2);
        dataInput = true;
    }

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
     style="margin-top: 2em"
     bind:this={vizElem}>
    {#if editing}
        <DiagramGroup group={diagram}>
        </DiagramGroup>
    {:else}
        <DiagramView group={diagram}>
        </DiagramView>
    {/if}
</div>

<div class="col-sm-6"
     style="margin-top: 2em">
    <div class="waltz-scroll-region-350">
        <pre>{html}</pre>
    </div>
</div>

<div class="col-sm-6"
     style="margin-top: 2em">
    <div class="waltz-scroll-region-350">
        {#if dataInput}
            <textarea class="form-control"
                      id="data"
                      placeholder="data"
                      rows="10"
                      bind:value={workingData}></textarea>
            <div class="help-block">
                Input data to populate diagram
            </div>
            <button class="btn btn-plain"
                    on:click={() => saveData()}>
                Done
            </button>
        {:else}
            <pre>{data}</pre>
            <button class="btn btn-plain"
                    on:click={editData}>
                Edit
            </button>
        {/if}
    </div>
</div>