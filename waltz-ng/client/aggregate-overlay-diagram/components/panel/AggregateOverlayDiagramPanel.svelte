<script>


    import AggregateOverlayDiagram from "../aggregate-overlay-diagram/AggregateOverlayDiagram.svelte";
    import {aggregateOverlayDiagramStore} from "../../../svelte-stores/aggregate-overlay-diagram-store";
    import {getContext, onMount} from "svelte";
    import WidgetSelector from "../aggregate-overlay-diagram/WidgetSelector.svelte";
    import {aggregateOverlayDiagramCalloutStore} from "../../../svelte-stores/aggregate-overlay-diagram-callout-store";
    import DiagramSelector from "../diagram-selector/DiagramSelector.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {setupContextStores} from "../aggregate-overlay-diagram/aggregate-overlay-diagram-utils";
    import _ from "lodash";
    import AggregateOverlayDiagramContextPanel from "../context-panel/AggregateOverlayDiagramContextPanel.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    export let primaryEntityRef;


    let svgCall;
    let calloutCall;
    let diagramsCall;


    const Modes = {
        SELECT: "SELECT",
        VIEW: "VIEW"
    }

    let activeMode = $selectedDiagram == null
        ? Modes.SELECT
        : Modes.VIEW

    onMount(() => {
        diagramsCall = aggregateOverlayDiagramStore.findAll();

    });


    $: {
        if ($selectedDiagram) {
            svgCall = aggregateOverlayDiagramStore.getById($selectedDiagram.id);
        }
    }

    $: {
        if ($selectedInstance) {
            calloutCall = aggregateOverlayDiagramCalloutStore.findCalloutsByDiagramInstanceId($selectedInstance.id);
        }
    }

    $: diagram = $svgCall?.data;
    $: diagrams = $diagramsCall?.data || [];


    function selectDiagram(evt) {
        $selectedInstance = null;
        $selectedDiagram = evt.detail;
        activeMode = Modes.VIEW;
    }


    setupContextStores();

    let selectedInstance = getContext("selectedInstance");
    let selectedDiagram = getContext("selectedDiagram");
    let diagramProportion = getContext("diagramProportion");


</script>

{#if primaryEntityRef}
    {#if _.isEmpty(diagrams)}
        <NoData>There are no diagrams</NoData>
    {:else}
        <div class="row">
            {#if activeMode === Modes.VIEW}
                <div class={`col-sm-${$diagramProportion}`}
                     style="padding-top: 1em">
                    <div class="col-sm-6">
                        <h4>
                            {$selectedDiagram?.name}
                            <button class="small btn btn-skinny pull-right"
                                    on:click={() => activeMode = Modes.SELECT}>
                                <Icon name="list-ul"/> Change diagram
                            </button>
                        </h4>

                    </div>
                    <div class="col-sm-6">
                        <div class="pull-right btn-group">
                            <button class="btn btn-default btn-xs"
                                    title="Expand Diagram"
                                    on:click={() => $diagramProportion = 12}>
                                <Icon name="arrows-alt"/>
                            </button>
                            <button class="btn btn-default btn-xs"
                                    title="Original Size"
                                    on:click={() => $diagramProportion = 9}>
                                <Icon name="chevron-left"/>
                            </button>
                            <button class="btn btn-default btn-xs"
                                    title="Expand Context Panel"
                                    on:click={() => $diagramProportion = 6}>
                                <Icon name="arrow-left"/>
                            </button>
                        </div>
                    </div>
                    <br>
                    <AggregateOverlayDiagram svg={$selectedDiagram?.svg}
                                             {primaryEntityRef}/>
                </div>
                <div class={`col-sm-${12 - $diagramProportion}`}
                     style="padding-left: 1em">
                    <AggregateOverlayDiagramContextPanel {primaryEntityRef}/>
                </div>
            {:else if activeMode === Modes.SELECT}
                <div class="col-sm-12">
                    <DiagramSelector {diagrams}
                                     on:select={selectDiagram}/>
                </div>
                <div class="col-sm-12"
                     style="padding-top: 1em">
                    <NoData>No diagram selected, choose one from the list above</NoData>
                </div>
            {/if}
        </div>
    {/if}
{/if}