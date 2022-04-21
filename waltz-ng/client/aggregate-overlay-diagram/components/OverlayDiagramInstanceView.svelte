<script>
    import PageHeader from "../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../common/svelte/ViewLink.svelte";
    import EntityLink from "../../common/svelte/EntityLink.svelte";
    import {aggregateOverlayDiagramInstanceStore} from "../../svelte-stores/aggregate-overlay-diagram-instance-store";
    import AggregateOverlayDiagram from "./aggregate-overlay-diagram/AggregateOverlayDiagram.svelte";
    import AggregateOverlayDiagramContextPanel
        from "./context-panel/AggregateOverlayDiagramInstanceContextPanel.svelte";
    import NoData from "../../common/svelte/NoData.svelte";
    import {aggregateOverlayDiagramStore} from "../../svelte-stores/aggregate-overlay-diagram-store";
    import {setupContextStores} from "./aggregate-overlay-diagram/aggregate-overlay-diagram-utils";
    import {aggregateOverlayDiagramCalloutStore} from "../../svelte-stores/aggregate-overlay-diagram-callout-store";

    export let primaryEntityRef;

    let diagramInstanceCall;
    let diagramCall;
    let calloutCall;

    $: {
        if (primaryEntityRef) {
            diagramInstanceCall = aggregateOverlayDiagramInstanceStore.getById(primaryEntityRef.id);
        }

        if ($selectedInstance) {
            diagramCall = aggregateOverlayDiagramStore.getById($selectedInstance.diagramId)
        }

        if ($selectedInstance) {
            calloutCall = aggregateOverlayDiagramCalloutStore.findCalloutsByDiagramInstanceId($selectedInstance.id);
        }
    }

    $: $selectedInstance = $diagramInstanceCall?.data;
    $: $selectedDiagram = $diagramCall?.data;
    $: $callouts = $calloutCall?.data;


    const {selectedDiagram, selectedInstance, callouts, hoveredCallout} = setupContextStores();

    $: console.log({si: $selectedInstance, diagram: $selectedDiagram});

</script>


<PageHeader icon="object-group"
            name={`Overlay Diagram Instance: ${$selectedInstance?.name}`}>
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li>
                <ViewLink state="main">Home</ViewLink>
            </li>
            <li>
                <ViewLink state="main.system.list">Diagram</ViewLink>
            </li>
            <li>
                <EntityLink ref={$selectedInstance}/>
            </li>
        </ol>
    </div>
</PageHeader>

<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="waltz-display-section">

        <div class="row">
            {#if $selectedInstance}
                <div class="col-sm-8">
                    <AggregateOverlayDiagram svg={$selectedInstance?.svg}
                                             {primaryEntityRef}/>
                </div>
                <div class="col-sm-4">
                    <AggregateOverlayDiagramContextPanel {primaryEntityRef}/>
                </div>
            {:else}
                <div class="col-sm-12" style="padding-top: 1em">
                    <NoData>No diagram selected</NoData>
                </div>
            {/if}
        </div>

    </div>
</div>