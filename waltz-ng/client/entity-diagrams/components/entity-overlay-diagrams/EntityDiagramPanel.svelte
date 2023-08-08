<script>

    import {diagramService, selectionOptions} from "./entity-diagram-store";
    import DiagramContextPanel from "./context-panel/DiagramContextPanel.svelte";
    import DiagramInteractView from "./DiagramInteractView.svelte";
    import {mkSelectionOptions} from "../../../common/selector-utils";
    import DiagramList from "./DiagramList.svelte";

    export let parentEntityRef;

    const {selectDiagram, overlayData, selectedOverlay, diagramLayout, selectedDiagram} = diagramService;

    $: $selectionOptions = mkSelectionOptions(parentEntityRef);

    function selectOverlayDiagram(evt) {
        selectDiagram(evt.detail.id);
    }

</script>

<div class="row">
    {#if $selectedDiagram}
        <div class="col-sm-8">
            <DiagramInteractView group={$diagramLayout}
                                 {parentEntityRef}>
            </DiagramInteractView>
        </div>
        <div class="col-sm-4">
            <DiagramContextPanel/>
        </div>
    {:else}
        <div class="col-sm-12">
            <DiagramList on:select={selectOverlayDiagram}/>
        </div>
    {/if}
</div>
