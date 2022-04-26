<script>


    import AggregateOverlayDiagram from "../aggregate-overlay-diagram/AggregateOverlayDiagram.svelte";
    import {aggregateOverlayDiagramStore} from "../../../svelte-stores/aggregate-overlay-diagram-store";
    import {onMount} from "svelte";
    import WidgetSelector from "../aggregate-overlay-diagram/WidgetSelector.svelte";
    import {aggregateOverlayDiagramInstanceStore} from "../../../svelte-stores/aggregate-overlay-diagram-instance-store";
    import {aggregateOverlayDiagramCalloutStore} from "../../../svelte-stores/aggregate-overlay-diagram-callout-store";
    import DiagramSelector from "../diagram-selector/DiagramSelector.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import DiagramInstanceSelector from "../instance-selector/DiagramInstanceSelector.svelte";
    import AggregateOverlayDiagramContextPanel
        from "../context-panel/AggregateOverlayDiagramInstanceContextPanel.svelte";
    import {setupContextStores} from "../aggregate-overlay-diagram/aggregate-overlay-diagram-utils";

    export let primaryEntityRef;

    let widgetComponent;

    function handleWidgetChange(e) {
        widgetComponent = e.detail.widget;
    }

    let svgCall;
    let instancesCall;
    let calloutCall;
    let diagramsCall;


    onMount(() => {
        diagramsCall = aggregateOverlayDiagramStore.findAll();

    });


    $: {
        if ($selectedDiagram) {
            svgCall = aggregateOverlayDiagramStore.getById($selectedDiagram.id);
            instancesCall = aggregateOverlayDiagramInstanceStore.findByDiagramId($selectedDiagram.id);
        }
    }

    $: {
        if ($selectedInstance) {
            calloutCall = aggregateOverlayDiagramCalloutStore.findCalloutsByDiagramInstanceId($selectedInstance.id);
        }
    }

    $: diagram = $svgCall?.data;
    $: instances = $instancesCall?.data || [];
    $: diagrams = $diagramsCall?.data || [];


    function selectDiagram(evt) {
        $selectedInstance = null;
        $selectedDiagram = evt.detail;
    }

    function selectInstance(evt) {
        $selectedInstance = evt.detail;
    }

    const {selectedDiagram, selectedInstance, hoveredCallout} = setupContextStores();


</script>

{#if primaryEntityRef}
    <div class="row">
        <div class="col-sm-3">
            <DiagramSelector {diagrams}
                             on:select={selectDiagram}/>
        </div>
        <div class="col-sm-3">
            {#if $selectedDiagram}
                <DiagramInstanceSelector {instances}
                                         on:select={selectInstance}/>
            {/if}
        </div>
    </div>
    <div class="row">
        {#if $selectedDiagram}
            <div class="col-sm-9" style="padding-top: 1em">
                <AggregateOverlayDiagram svg={$selectedDiagram?.svg}
                                         {primaryEntityRef}
                                         {widgetComponent}/>
            </div>
            <div class="col-sm-3">
                <div>
                    <WidgetSelector {primaryEntityRef}/>
                </div>
                <AggregateOverlayDiagramContextPanel {handleWidgetChange}
                                                     {primaryEntityRef}/>
            </div>
        {:else}
            <div class="col-sm-12" style="padding-top: 1em">
                <NoData>No diagram selected, choose one from the list above</NoData>
            </div>
        {/if}
    </div>
{/if}