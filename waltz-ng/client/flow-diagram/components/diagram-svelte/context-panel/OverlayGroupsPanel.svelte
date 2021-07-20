<script>
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {createEventDispatcher} from "svelte";
    import AddOverlayGroupEntrySubPanel from "./AddOverlayGroupEntrySubPanel.svelte";
    import overlay from "../store/overlay";
    import {flowDiagramOverlayGroupStore} from "../../../../svelte-stores/flow-diagram-overlay-group-store";
    import {toGraphId} from "../../../flow-diagram-utils";
    import OverlayGlyph from "./OverlayGlyph.svelte";
    import EditOverlayIconSubPanel from "./EditOverlayIconSubPanel.svelte";
    import CreateNewOverlayGroupPanel from "./CreateNewOverlayGroupPanel.svelte";
    import CloneOverlayGroupSubPanel from "./CloneOverlayGroupSubPanel.svelte";
    import EntityLabel from "../../../../common/svelte/EntityLabel.svelte";

    const Modes = {
        LIST_GROUPS: "LIST_GROUPS",
        SHOW_GROUP: "SHOW_GROUP",
        SHOW_ENTRY: "SHOW_ENTRY",
        ADD_GROUP: "ADD_GROUP",
        CLONE_GROUP: "CLONE_GROUP",
        ADD_OVERLAY_ENTRY: "ADD_OVERLAY_ENTRY",
        EDIT_OVERLAY: "EDIT_OVERLAY",
        CONFIRM_REMOVE_GROUP: "CONFIRM_REMOVE_GROUP"
    };

    const dispatch = createEventDispatcher();

    export let diagramId;
    export let canEdit;

    let selectedOverlay;
    let overlayEntries = [];
    let removePromise;
    let activeMode = Modes.LIST_GROUPS;
    let breadcrumbs = [];

    $: {
        if (!$overlay.selectedGroup) {
            activeMode = Modes.LIST_GROUPS;
        }
        if ($overlay.selectedGroup) {
            if (selectedOverlay) {
                activeMode = Modes.SHOW_ENTRY;
            } else {
                activeMode = Modes.SHOW_GROUP;
            }
        }
    }

    function cancel() {
        overlay.clearSelectedGroup();
    }

    function showGroupList() {
        overlay.clearSelectedGroup();
    }

    function selectOverlayGroup(group) {
        overlay.setSelectedGroup(group);
    }

    function setIndividualOverlay(groupOverlay) {
        overlay.setAppliedOverlay(groupOverlay);
    }

    function clearIndividualOverlay() {
        overlay.clearAppliedOverlay();
    }

    function removeOverlayGroup(group) {
        return removePromise = flowDiagramOverlayGroupStore
            .deleteGroup(diagramId, group.data.id)
            .then(showGroupList);
    }

    function editOverlayEntry(overlayEntry) {
        selectedOverlay = overlayEntry.data;
        activeMode = Modes.EDIT_OVERLAY
    }

    $: overlayGroupsCall = flowDiagramOverlayGroupStore.findByDiagramId(diagramId);

    $: overlayGroups = _.map(
        $overlayGroupsCall.data,
        d => ({
            id: toGraphId({kind: 'GROUP', id: d.id}),
            data: d
        }));

    $: {
        breadcrumbs =_
            .chain([
                {name: "Groups", onClick: showGroupList},
                $overlay.selectedGroup
                    ? {name: $overlay.selectedGroup.data.name, onClick: () => selectedOverlay = null}
                    : null,
                selectedOverlay ? {name: selectedOverlay.entityReference.name} : null
            ])
            .compact()
            .map(d => Object.assign({}, d, {active: false}))
            .value();

        _.last(breadcrumbs).active = true;
    }

    $: overlayEntries = _.get($overlay.groupOverlays, $overlay.selectedGroup?.id, [])
</script>


<p class="help-block">
    Overlay groups can be used to show relationships between nodes and other Waltz entities
</p>

<ol class="breadcrumb">
    {#each breadcrumbs as crumb}
        {#if crumb.active}
            <li class="active">{crumb.name}</li>
        {:else}
            <li>
                <button style="padding: 0"
                        class="btn-skinny"
                        on:click={crumb.onClick}>
                    {crumb.name}
                </button>
            </li>
        {/if}
    {/each}
</ol>

<div style="padding-left: 1em;">
    {#if activeMode === Modes.LIST_GROUPS}
        <ul class="list-unstyled">
            {#each overlayGroups as group}
                <li>
                    <button class="btn-skinny"
                            on:click={() => selectOverlayGroup(group)}>
                        <Icon name="star-o"/>
                        {group.data.name}
                    </button>
                </li>
            {/each}
            <li style="border-top: 1px dotted #eee; padding-top: 0.2em; margin-top: 0.2em">
                <button class="btn-skinny"
                        on:click={() => activeMode = Modes.ADD_GROUP}>
                    <Icon name="plus"/>
                    Add new overlay group
                </button>
            </li>
            <li>
                <button class="btn-skinny"
                        on:click={() => activeMode = Modes.CLONE_GROUP}>
                    <Icon name="clone"/>
                    Clone existing overlay group
                </button>
            </li>
        </ul>
    {/if}

    {#if activeMode === Modes.ADD_GROUP}
        <h4>Create Group:</h4>
        <CreateNewOverlayGroupPanel {diagramId}
                                    on:cancel={() => activeMode = Modes.LIST_GROUPS}/>
    {/if}

    {#if activeMode === Modes.CLONE_GROUP}
        <h4>Clone Group from:</h4>
        <CloneOverlayGroupSubPanel {diagramId}
                                   on:cancel={() => activeMode = Modes.LIST_GROUPS}/>
    {/if}

    {#if activeMode === Modes.SHOW_GROUP}
        <ul class="list-unstyled">
            {#each _.sortBy(overlayEntries, g => g.data.entityReference.name) as groupOverlay}
                <li class="waltz-visibility-parent"
                    on:mouseenter={() => setIndividualOverlay(groupOverlay)}
                    on:mouseleave={() => clearIndividualOverlay()}>
                    <span style="padding-left: 0.4em">
                        <OverlayGlyph overlay={groupOverlay.data}/>
                        &nbsp;
                        <button class="btn-skinny"
                                on:click={() => editOverlayEntry(groupOverlay)}>
                            <EntityLabel ref={groupOverlay.data.entityReference}/>
                        </button>
                    </span>
                </li>
            {:else}
                <li>
                    <Icon name="info-circle"/>
                    You have no overlays added to this group; these can be used to group/filter applications.
                </li>
            {/each}
            {#if canEdit}
                <li style="border-top: 1px dotted #eee; padding-top: 0.2em; margin-top: 0.2em">
                    <button class="btn btn-skinny"
                            on:click={() => activeMode = Modes.ADD_OVERLAY_ENTRY}>
                        <Icon name="plus"/>
                        Add overlay item
                    </button>
                </li>
                <li>
                    <button class="btn btn-skinny"
                            on:click={() => activeMode = Modes.CONFIRM_REMOVE_GROUP}>
                        <Icon name="trash"/>
                        Remove Group
                    </button>
                </li>
            {/if}
        </ul>
    {/if}

    {#if activeMode === Modes.CONFIRM_REMOVE_GROUP}
        <div>
            <Icon name="warning"/>
            Are you sure you want to remove this group and clear the associated items ?
        </div>
        <button class="btn btn-danger"
                on:click={() => removeOverlayGroup($overlay.selectedGroup)}>
            OK
        </button>
        <button class="btn btn-default"
                on:click={() => activeMode = Modes.SHOW_GROUP}>
            Cancel
        </button>
    {/if}

    {#if activeMode === Modes.SHOW_ENTRY}
        <EditOverlayIconSubPanel group={$overlay.selectedGroup}
                                 {canEdit}
                                 on:cancel={() => selectedOverlay = null}
                                 selectedOverlay={selectedOverlay}/>
    {/if}
    {#if activeMode === Modes.ADD_OVERLAY_ENTRY}
        <AddOverlayGroupEntrySubPanel group={$overlay.selectedGroup}
                                      on:cancel={() => activeMode = Modes.SHOW_GROUP}
                                      {diagramId}
                                      {canEdit}
                                      overlays={overlayEntries}/>
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