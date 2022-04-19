<script>


    import AggregateOverlayDiagram from "../aggregate-overlay-diagram/AggregateOverlayDiagram.svelte";
    import {aggregateOverlayDiagramStore} from "../../../svelte-stores/aggregate-overlay-diagram-store";
    import {onMount} from "svelte";
    import WidgetSelector from "../aggregate-overlay-diagram/WidgetSelector.svelte";
    import {aggregateOverlayDiagramInstanceStore} from "../../../svelte-stores/aggregate-overlay-diagram-instance-store";
    import {aggregateOverlayDiagramCalloutStore} from "../../../svelte-stores/aggregate-overlay-diagram-callout-store";
    import DiagramSelector from "../diagram-selector/DiagramSelector.svelte";
    import {selectedDiagram, selectedInstance, callouts} from "../../aggregate-overlay-diagram-store";
    import NoData from "../../../common/svelte/NoData.svelte";
    import DiagramInstanceSelector from "../instance-selector/DiagramInstanceSelector.svelte";
    import CalloutList from "../aggregate-overlay-diagram/CalloutList.svelte";
    import AggregateOverlayDiagramContextPanel from "../context-panel/AggregateOverlayDiagramContextPanel.svelte";

    export let svg = '<svg><circle r="100" fill="red"/></svg>';
    export let primaryEntityRef;

    let widgetComponent;
    let dataProvider;

    function handleWidgetChange(e) {
        widgetComponent = e.detail.widget;
        dataProvider = e.detail.dataProvider;
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
    $: instances = $instancesCall?.data;
    $: $callouts = $calloutCall?.data;
    $: diagrams = $diagramsCall?.data;

    $: console.log({svg, primaryEntityRef, instances, callouts});


    function selectDiagram(evt) {
        console.log({diagram: evt.detail});
        $selectedInstance = null;
        $callouts = [];
        $selectedDiagram = evt.detail;
    }

    function selectInstance(evt) {
        console.log({instance: evt.detail});
        $selectedInstance = evt.detail;
    }

</script>

{#if primaryEntityRef}
    <div class="row">
        <div class="col-sm-4">
            <DiagramSelector {diagrams}
                             on:select={selectDiagram}/>
        </div>
        <div class="col-sm-4">
            {#if $selectedDiagram}
                <DiagramInstanceSelector {instances}
                                         on:select={selectInstance}/>
            {/if}
        </div>
    </div>
    <div class="row">
        {#if $selectedDiagram}
            <div class="col-sm-9">
                <AggregateOverlayDiagram svg={$selectedDiagram?.svg}
                                         {primaryEntityRef}
                                         {widgetComponent}
                                         {dataProvider}/>
            </div>
            <div class="col-sm-3">
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