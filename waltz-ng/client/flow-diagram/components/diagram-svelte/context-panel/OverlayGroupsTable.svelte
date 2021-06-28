<script>
    import Icon from "../../../../common/svelte/Icon.svelte";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import _ from "lodash";
    import {createEventDispatcher} from "svelte";
    import AddOverlayGroupEntrySubPanel from "./AddOverlayGroupEntrySubPanel.svelte";
    import overlay from "../store/overlay";
    import {flowDiagramOverlayGroupStore} from "../../../../svelte-stores/flow-diagram-overlay-group-store";
    import {toGraphId} from "../../../flow-diagram-utils";
    import OverlayGlyph from "./OverlayGlyph.svelte";
    import EditOverlayIconSubPanel from "./EditOverlayIconSubPanel.svelte";

    const Modes = {
        TABLE: "TABLE",
        ADD_OVERLAY: "ADD_OVERLAY",
        EDIT_OVERLAY: "EDIT_OVERLAY",
    };

    const dispatch = createEventDispatcher();

    export let diagramId;
    export let canEdit;

    let selectedGroup;
    let selectedOverlay;
    let groupOverlays;
    let removePromise;
    let activeMode = Modes.TABLE;


    function cancel() {
        activeMode = Modes.TABLE;
    }

    function selectRow(group) {
        if (selectedGroup === group) {
            overlay.clearSelectedGroup();
        } else {
            overlay.setSelectedGroup(group);
        }
        selectedGroup = (selectedGroup === group) ? null : group;
    }

    function setOverlay(groupOverlay) {
        overlay.setAppliedOverlay(groupOverlay);
    }

    function clearOverlay() {
        overlay.clearAppliedOverlay();
    }

    function removeOverlay(groupOverlay) {
        overlay.removeOverlay(groupOverlay);
    }

    function removeOverlayGroup(group) {
        return removePromise = flowDiagramOverlayGroupStore
            .deleteGroup(diagramId, group.data.id);
    }

    function editOverlay(overlayGroup) {
        selectedOverlay = overlayGroup.data;
        activeMode = Modes.EDIT_OVERLAY
    }

    $: overlayGroupsCall = flowDiagramOverlayGroupStore.findByDiagramId(diagramId);
    $: overlayGroups = _.map(
        $overlayGroupsCall.data,
        d => ({
            id: toGraphId({kind: 'GROUP', id: d.id}),
            data: d
        }));

    $: groupOverlays = selectedGroup && _.get($overlay.groupOverlays, selectedGroup?.id, []);
</script>


<div>
    {#if  activeMode === Modes.TABLE}
        <p class="help-block">Overlay groups can be used to show relationships between nodes and other Waltz entities</p>
        {#if _.size(overlayGroups) === 0 }
            No overlay groups have been created for this diagram.
        {:else}
        <table class="table table-condensed small">
        <thead>
        <tr>
            <th width="5%"></th>
            <th width="40%">Name</th>
            <th width="40%">Description</th>
        </tr>
        </thead>
        <tbody>
        {#each overlayGroups as group}
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
            </tr>
            {#if selectedGroup === group}
                <tr class="env-detail-row">
                    <td></td>
                    <td colspan="3">
                        {#if _.size(groupOverlays) === 0}
                            You have no overlays added to this group; these can be used to group/filter applications.
                        {:else }
                            <ul>
                            {#each _.sortBy(groupOverlays, g => g.data.entityReference.name) as groupOverlay}
                                <li class="waltz-visibility-parent"
                                    on:mouseenter={() =>  setOverlay(groupOverlay)}
                                    on:mouseleave={() => clearOverlay()}>
                                    <OverlayGlyph overlay={groupOverlay.data}/>
                                    &nbsp;
                                    <EntityLink ref={groupOverlay.data.entityReference}/>
                                    {#if canEdit}
                                    <button class="btn btn-skinny waltz-visibility-child-30"
                                            on:click={() => removeOverlay(groupOverlay)}>
                                        <Icon name="trash"/>
                                    </button>
                                    <button class="btn btn-skinny waltz-visibility-child-30"
                                            on:click={() => editOverlay(groupOverlay)}>
                                        <Icon name="pencil"/>
                                    </button>
                                    {/if}
                                </li>
                            {/each}
                            </ul>
                        {/if}
                        {#if canEdit}
                        <br>
                        <button class="btn btn-skinny"
                                on:click={() => activeMode = Modes.ADD_OVERLAY}>
                            <Icon name="plus"/>
                            Add overlay
                        </button>
                        |
                        <button class="btn btn-skinny"
                                on:click={() => removeOverlayGroup(group)}>
                            <Icon name="trash"/>
                            Remove Group
                        </button>
                        {/if}
                    </td>
                </tr>
            {/if}
        {/each}
        </tbody>
    </table>
    {/if}
    {:else if activeMode === Modes.ADD_OVERLAY}
        <AddOverlayGroupEntrySubPanel {diagramId}
                                      group={selectedGroup}
                                      on:cancel={cancel}
                                      overlays={groupOverlays}/>
        <br>
    {:else if activeMode === Modes.EDIT_OVERLAY}
        <EditOverlayIconSubPanel  group={selectedGroup}
                                  on:cancel={cancel}
                                  selectedOverlay={selectedOverlay}/>
        <br>
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