<script>

    import {diagramService, selectionOptions} from "./entity-diagram-store";
    import DiagramContextPanel from "./context-panel/DiagramContextPanel.svelte";
    import DiagramInteractView from "./DiagramInteractView.svelte";
    import {mkSelectionOptions} from "../../../common/selector-utils";
    import DiagramList from "./DiagramList.svelte";
    import {aggregateOverlayDiagramStore} from "../../../svelte-stores/aggregate-overlay-diagram-store";
    import {overlayDiagramKind} from "../../../common/services/enums/overlay-diagram-kind";
    import {releaseLifecycleStatus} from "../../../common/services/enums/release-lifecycle-status";
    import _ from "lodash";

    export let parentEntityRef;

    const {selectDiagram, overlayData, selectedOverlay, diagramLayout, selectedDiagram} = diagramService;

    const diagramsCall = aggregateOverlayDiagramStore.findByKind(overlayDiagramKind.WALTZ_ENTITY_OVERLAY.key, true);
    $: diagrams = _.filter($diagramsCall.data, d => d.status === releaseLifecycleStatus.ACTIVE.key) || [];

    $: $selectionOptions = mkSelectionOptions(parentEntityRef);

    function selectOverlayDiagram(evt) {
        selectDiagram(evt.detail.id);
    }

</script>

<div class="row waltz-sticky-wrapper">
    {#if $selectedDiagram}
        <div class="col-sm-8">
            <DiagramInteractView group={$diagramLayout}
                                 {parentEntityRef}>
            </DiagramInteractView>
        </div>
        <div class="col-sm-4">
            <div class="waltz-sticky-part">
                <DiagramContextPanel/>
            </div>
        </div>
    {:else}
        <div class="col-sm-12">
            <DiagramList {diagrams}
                         on:select={selectOverlayDiagram}/>
        </div>
    {/if}
</div>
