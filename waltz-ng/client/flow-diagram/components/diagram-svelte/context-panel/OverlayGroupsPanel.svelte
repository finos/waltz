<script>
    import {store} from "../diagram-model-store";
    import {mkRef} from "../../../../common/entity-utils";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import {measurableCategoryAlignmentViewStore} from "../../../../svelte-stores/measurable-category-alignment-view-store";
    import {flowDiagramOverlayGroupStore} from "../../../../svelte-stores/flow-diagram-overlay-group-store";
    import OverlayGroupsTable from "./OverlayGroupsTable.svelte";

    let workingGroup;
    export let diagramId;

    const Modes = {
        TABLE: "TABLE",
        ADD_GROUP: "ADD_GROUP",
    };

    let activeMode = Modes.TABLE

    $: measurableAlignmentCall = measurableCategoryAlignmentViewStore
        .findAlignmentsByAppSelectorRoute(mkSelectionOptions(mkRef('FLOW_DIAGRAM', diagramId)));

    $: alignments = $measurableAlignmentCall.data;

    $: overlayGroupCall = flowDiagramOverlayGroupStore.findByDiagramId(diagramId);
    $: overlays = $overlayGroupCall.data

    $: overlayGroups = $store.model.groups;

    function cancel() {
        workingGroup = null;
    }

</script>

{#if activeMode === Modes.TABLE}
    <OverlayGroupsTable {diagramId} {alignments}/>
{:else if activeMode === Modes.ADD_GROUP}
    <h4>Add group</h4>
{/if}

<style>

</style>