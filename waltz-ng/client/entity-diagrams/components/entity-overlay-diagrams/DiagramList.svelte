<script>

    import {createEventDispatcher} from "svelte";
    import {aggregateOverlayDiagramStore} from "../../../svelte-stores/aggregate-overlay-diagram-store";
    import {overlayDiagramKind} from "../../../common/services/enums/overlay-diagram-kind";

    const dispatch = createEventDispatcher();

    const diagramsCall = aggregateOverlayDiagramStore.findByKind(overlayDiagramKind.WALTZ_ENTITY_OVERLAY.key);
    $: diagrams = $diagramsCall.data || [];

    function selectDiagram(diagram) {
        dispatch("select", diagram);
    }

</script>


<table class="table table-condensed table-hover">
    <thead>
    <tr>
        <th>Name</th>
        <th>Description</th>
        <th>Aggregated Entity Kind</th>
    </tr>
    </thead>
    <tbody>
    {#each diagrams as diagram}
        <tr on:click={() => selectDiagram(diagram)}
            class="clickable">
            <td>{diagram.name}</td>
            <td>{diagram.description || '-'}</td>
            <td>{diagram.aggregatedEntityKind}</td>
        </tr>
    {:else}
        <tr>
            <td colspan="2">There are no entity overlay diagrams</td>
        </tr>
    {/each}
    </tbody>
</table>
