<script>

    import {diagramLayoutData} from "./test-data/layout-data";
    import _ from "lodash";
    import {diagramService, selectionOptions} from "./entity-diagram-store";
    import DiagramContextPanel from "./DiagramContextPanel.svelte";
    import DiagramInteractView from "./DiagramInteractView.svelte";
    import {mkSelectionOptions} from "../../../common/selector-utils";
    import DiagramList from "./DiagramList.svelte";
    import {onMount} from "svelte";
    import {overlays} from "./entity-diagram-utils";

    export let parentEntityRef;

    let diagrams = [{id: 1, name: "Test Data", description: "Test diagram for dev", layoutData: diagramLayoutData, lastUpdatedBy: "admin", lastUpdatedAt: new Date(), provenance: "test"}];

    const {selectDiagram, overlayData, selectedOverlay, diagramLayout, selectedDiagram, selectOverlay} = diagramService;

    onMount(() => {
        selectOverlay(_.first(overlays));
    })

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
