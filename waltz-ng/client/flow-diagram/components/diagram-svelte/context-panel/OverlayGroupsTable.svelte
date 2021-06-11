<script>
    import Icon from "../../../../common/svelte/Icon.svelte";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import _ from "lodash";
    import {createEventDispatcher} from "svelte";
    import AddOverlayGroupEntrySubPanel from "./AddOverlayGroupEntrySubPanel.svelte";
    import overlay from "../store/overlay";


    export let diagramId;
    export let alignments;

    let selectedGroup;
    let groupOverlays;

    const Modes = {
        TABLE: "TABLE",
        ADD_OVERLAY: "ADD_OVERLAY",
    };

    let activeMode = Modes.TABLE;

    const dispatch = createEventDispatcher();

    function cancel() {
        activeMode = Modes.TABLE;
    }

    // $: overlayGroupCall = flowDiagramOverlayGroupStore.findByDiagramId(diagramId);
    // $: overlayGroups = $overlayGroupCall.data


    // $: overlaysCall = flowDiagramOverlayGroupStore.findOverlaysByDiagramId(diagramId);
    // $: overlaysByGroupId = _.groupBy($overlaysCall.data, d => d.overlayGroupId);


    function selectRow(group) {
        if(selectedGroup === group){
            overlay.clearSelectedGroup();
        } else {
            // overlay.setSelectedGroup({id: toGraphId({kind: 'GROUP', id: group.id}), data: group})
            overlay.setSelectedGroup(group);
        }
        selectedGroup = (selectedGroup === group) ? null : group;
    }

    $: groupOverlays = selectedGroup && _.get($overlay.groupOverlays, selectedGroup?.id, []);
    // $: groupOverlays = selectedGroup && _.get($overlay.groupOverlays, toGraphId({kind: 'GROUP', id: selectedGroup.id}), []);

    function setOverlay(groupOverlay) {
        overlay.setAppliedOverlay(groupOverlay);
    }

    function clearOverlay() {
        overlay.clearAppliedOverlay();
    }

    function removeOverlay(groupOverlay) {
        overlay.removeOverlay(groupOverlay);
    }

</script>


<div>
    {#if  activeMode === Modes.TABLE}
        <h4>Groups:</h4>
        <table class="table table-condensed small">
        <thead>
        <tr>
            <th width="5%"></th>
            <th width="40%">Name</th>
            <th width="40%">Description</th>
            <th width="15%"># Overlays</th>
        </tr>
        </thead>
        <tbody>
        {#each $overlay.groups as group}
            <tr class="clickable"
                on:click={() => selectRow(group)}>
                <td>
                    <Icon size="lg"
                          name={selectedGroup === group
                                ? "caret-down"
                                : "caret-right"}/>
                </td>
                <td>{group.data.name}</td>
                <td>{group.data.description || "-"}</td>
                <td>0</td>
            </tr>
            {#if selectedGroup === group}
                <tr class="env-detail-row">
                    <td></td>
                    <td colspan="3">
                        {#if _.size(groupOverlays) === 0}
                            You have no overlays added to this group; these can be used to group/filter applications.
                        {:else }
                            <ul>
                            {#each groupOverlays as groupOverlay}
                                <li on:mouseenter={() =>  setOverlay(groupOverlay)}
                                    on:mouseleave={() => clearOverlay()}>
                                    <EntityLink ref={groupOverlay.data.entityReference}/> ({groupOverlay.data.symbol}/{groupOverlay.data.fill})
                                    <button class="btn btn-skinny"
                                            on:click={() => removeOverlay(groupOverlay)}>
                                        <Icon name="trash"/>
                                    </button>
                                </li>
                            {/each}
                            </ul>
                        {/if}
                        <br>
                        <button class="btn btn-skinny"
                                on:click={() => activeMode = Modes.ADD_OVERLAY}>
                            <Icon name="plus"/>
                            Add overlay
                        </button>
                    </td>
                </tr>
            {/if}
        {/each}
        </tbody>
    </table>
    {:else if activeMode === Modes.ADD_OVERLAY}
        <h4>Adding overlay for {selectedGroup.name}:</h4>
        <AddOverlayGroupEntrySubPanel {alignments}
                                      group={selectedGroup}
                                      on:cancel={cancel}
                                      overlays={groupOverlays}/>
    {/if}
</div>

<style>
    ul {
        padding: 0;
        margin: 0;
        list-style: none;
    }

    li {
    padding-top: 0;
    }
</style>