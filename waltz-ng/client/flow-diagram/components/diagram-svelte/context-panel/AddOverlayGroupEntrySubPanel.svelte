<script>
    import GroupSelectorPanel from "./GroupSelectorPanel.svelte";
    import {createEventDispatcher} from "svelte";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import {getNewOverlay} from "./group-utils";
    import {measurableCategoryAlignmentViewStore} from "../../../../svelte-stores/measurable-category-alignment-view-store";
    import {mkRef} from "../../../../common/entity-utils";
    import EntitySearchSelector from "../../../../common/svelte/EntitySearchSelector.svelte";
    import EditOverlayIconSubPanel from "./EditOverlayIconSubPanel.svelte";

    export let group;
    export let diagramId;
    export let overlays;
    export let canEdit;

    let workingOverlayEntity;

    const Modes = {
        ADD_OVERLAY: "ADD_OVERLAY",
        SELECT_MEASURABLE: "SELECT_MEASURABLE",
        SELECT_APP_GROUP: "SELECT_APP_GROUP"
    }

    let activeMode = Modes.SELECT_MEASURABLE

    $: measurableAlignmentCall = measurableCategoryAlignmentViewStore
        .findAlignmentsByAppSelectorRoute(mkSelectionOptions(mkRef('FLOW_DIAGRAM', diagramId)));
    $: alignments = $measurableAlignmentCall.data;

    const dispatch = createEventDispatcher();

    function cancel() {
        workingOverlayEntity = null;
        submit();
    }

    function submit() {
        dispatch("cancel");
    }

    function selectOverlayEntity(e) {
        workingOverlayEntity = e.detail;
        activeMode = Modes.ADD_OVERLAY;
    }


    $: newOverlay = workingOverlayEntity && getNewOverlay(workingOverlayEntity, overlays);

</script>


<div>
    {#if activeMode === Modes.SELECT_MEASURABLE && alignments}
        <strong>Adding measurable overlay for {group.data.name}:</strong>
        <div style="padding-top: 1em">
            <GroupSelectorPanel on:select={selectOverlayEntity} {alignments}/>
            <button on:click={() => activeMode = Modes.SELECT_APP_GROUP}
                    class="btn btn-skinny">
                ...or add an application group overlay
            </button>
        </div>
        <div class="context-panel-footer">
            <button on:click={cancel}
                    class="btn btn-skinny">
                Cancel
            </button>
        </div>
    {:else if activeMode === Modes.SELECT_APP_GROUP}
        <strong>Adding application group overlay for {group.data.name}:</strong>
        <div style="padding-top: 1em">
            <EntitySearchSelector on:select={selectOverlayEntity}
                                  placeholder="Search for app group"
                                  entityKinds={['APP_GROUP']}>
            </EntitySearchSelector>
            <div>
                <button on:click={() => activeMode = Modes.SELECT_MEASURABLE}
                    class="btn btn-skinny">
                ...or add a measurable overlay
                </button>
            </div>
        </div>
        <div class="context-panel-footer">
            <button on:click={cancel}
                    class="btn btn-skinny">
                Cancel
            </button>
        </div>
    {:else if activeMode === Modes.ADD_OVERLAY}
        <strong>Editing overlay for group: {group.data.name}:</strong>
        <EditOverlayIconSubPanel selectedOverlay={newOverlay}
                                 {canEdit}
                                 {group}
                                 on:cancel={cancel}/>
    {/if}
</div>

<style>
    .context-panel-footer {
        border-top: 1px solid #eee;
        margin-top:0.5em;
        padding-top:0.5em;
    }
</style>