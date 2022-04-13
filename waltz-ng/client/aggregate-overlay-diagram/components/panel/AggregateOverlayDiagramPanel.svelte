<script>


    import AggregateOverlayDiagram from "../aggregate-overlay-diagram/AggregateOverlayDiagram.svelte";
    import {aggregateOverlayDiagramStore} from "../../../svelte-stores/aggregate-overlay-diagram-store";
    import {onMount} from "svelte";
    import WidgetSelector from "../aggregate-overlay-diagram/WidgetSelector.svelte";

    export let svg = '<svg><circle r="100" fill="red"/></svg>';
    export let primaryEntityRef;

    let widgetComponent;
    let dataProvider;

    function handleWidgetChange(e) {
        widgetComponent = e.detail.widget;
        dataProvider = e.detail.dataProvider;
    }

    let svgCall;


    onMount(() => {
        svgCall = aggregateOverlayDiagramStore.getById(1);
    });

    $: diagram = $svgCall?.data;

    $: console.log({svg, primaryEntityRef});

</script>

{#if primaryEntityRef}
    <div class="row">
        <div class="col-sm-9">
            <AggregateOverlayDiagram svg={diagram?.svg}
                                     {primaryEntityRef}
                                     {widgetComponent}
                                     {dataProvider}/>
        </div>
        <div class="col-sm-3">
            <WidgetSelector on:change={handleWidgetChange}
                            {primaryEntityRef}/>
        </div>
    </div>
{/if}