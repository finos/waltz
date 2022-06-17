<script>
    import AggregateOverlayDiagram from "../aggregate-overlay-diagram/AggregateOverlayDiagram.svelte";
    import {aggregateOverlayDiagramStore} from "../../../svelte-stores/aggregate-overlay-diagram-store";
    import {getContext, onMount} from "svelte";
    import {aggregateOverlayDiagramCalloutStore} from "../../../svelte-stores/aggregate-overlay-diagram-callout-store";
    import DiagramSelector from "../diagram-selector/DiagramSelector.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {
        determineWhichCellsAreLinkedByParent,
        setupContextStores,
        widgets
    } from "../aggregate-overlay-diagram/aggregate-overlay-diagram-utils";
    import _ from "lodash";
    import AggregateOverlayDiagramContextPanel from "../context-panel/AggregateOverlayDiagramContextPanel.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {measurableRelationshipStore} from "../../../svelte-stores/measurable-relationship-store";
    import {settingsStore} from "../../../svelte-stores/settings-store";

    export let primaryEntityRef;

    const Modes = {
        SELECT: "SELECT",
        VIEW: "VIEW"
    }

    setupContextStores();

    let selectedInstance = getContext("selectedInstance");
    let selectedDiagram = getContext("selectedDiagram");
    let diagramProportion = getContext("diagramProportion");
    let cellIdsExplicitlyRelatedToParent = getContext("cellIdsExplicitlyRelatedToParent");
    let focusWidget = getContext("focusWidget");
    let diagramPresets = getContext("diagramPresets");
    let overlayDataCall = getContext("overlayDataCall");
    let loading = getContext("loading");
    let disabledWidgetKeys = getContext("disabledWidgetKeys");

    let svgCall;
    let calloutCall;
    let diagramsCall;
    let relatedEntitiesCall;
    let presetsCall;
    let settingsCall;

    let disabledWidgetsSetting;

    function clearWidgetParameters() {
        $focusWidget = null;
        _.each(widgets, d => d.resetParameters ? d.resetParameters() : null);
    }

    function selectDiagram(evt) {
        clearWidgetParameters();
        $selectedInstance = null;
        $selectedDiagram = evt.detail;
        activeMode = Modes.VIEW;
    }

    let activeMode = $selectedDiagram == null
        ? Modes.SELECT
        : Modes.VIEW

    onMount(() => {
        diagramsCall = aggregateOverlayDiagramStore.findAll();
        settingsCall = settingsStore.loadAll();
    });

    $: {
        if ($selectedDiagram) {
            svgCall = aggregateOverlayDiagramStore.getById($selectedDiagram.id);
            relatedEntitiesCall = measurableRelationshipStore.findByEntityReference(primaryEntityRef);
            presetsCall = aggregateOverlayDiagramStore.findPresetsForDiagram($selectedDiagram.id);
        }
    }

    $: {
        if ($selectedInstance) {
            calloutCall = aggregateOverlayDiagramCalloutStore.findCalloutsByDiagramInstanceId($selectedInstance.id);
        }
    }


    $: diagram = $svgCall?.data?.diagram;
    $: backingEntities = $svgCall?.data?.backingEntities;
    $: diagrams = $diagramsCall?.data || [];
    $: relatedEntities = $relatedEntitiesCall?.data;
    $: $cellIdsExplicitlyRelatedToParent = determineWhichCellsAreLinkedByParent(backingEntities, relatedEntities);

    $: $diagramPresets = _.filter(
        $presetsCall?.data,
        p => {
            const overlayConfig = JSON.parse(p.overlayConfig);
            return !_.some($disabledWidgetKeys, k => k === overlayConfig?.widgetKey)
        });

    $: disabledWidgetsSetting = _.find(
        $settingsCall?.data,
        d => d.name === "feature.overlay-diagrams.disabled-widget-keys");

    $: $disabledWidgetKeys = _.isNull(disabledWidgetsSetting)
        ? []
        : _.map(_.split(disabledWidgetsSetting?.value, ","), s => _.trim(s));


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
                            <button class="small btn btn-link"
                                    on:click={() => activeMode = Modes.SELECT}>
                                <Icon name="list-ul"/>
                                Change diagram
                            </button>
                        </h4>

                    </div>
                    <div class="col-sm-4">
                        {#if $loading}
                            <h4>
                                Loading
                                <Icon name="refresh" spin="true"/>
                            </h4>
                        {/if}
                    </div>
                    <div class="col-sm-2">
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