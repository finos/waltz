<script>
    import {
        arcs,
        categories,
        clearSelections,
        clients,
        clientScale,
        clientScrollOffset,
        flowDirection,
        flowDirections,
        focusClient,
        layout,
        selectedClient
    } from "./flow-decorator-store";
    import Categories from "./Categories.svelte";
    import Clients from "./Clients.svelte";
    import {dimensions, mkArcs, mkCategories, mkClients, summariseFlows} from "./flow-decorator-utils";
    import _ from "lodash";
    import Arcs from "./Arcs.svelte";
    import {event, select} from "d3-selection";
    import {zoom} from "d3-zoom";
    import {logicalFlowStore} from "../../../svelte-stores/logical-flow-store";
    import FlowContextPanel from "./FlowContextPanel.svelte";
    import {applicationStore} from "../../../svelte-stores/application-store";
    import {actorStore} from "../../../svelte-stores/actor-store";
    import {physicalFlowStore} from "../../../svelte-stores/physical-flow-store";
    import {mkSelectionOptions} from "../../../common/selector-utils";
    import Icon from "../../../common/svelte/Icon.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {flowClassificationStore} from "../../../svelte-stores/flow-classification-store";

    export let primaryEntityRef;

    let flowGraphSummaryCall = logicalFlowStore.getFlowGraphSummary(primaryEntityRef, null);
    let flowClassificationCall = flowClassificationStore.findAll()
    let physicalFlowCall = physicalFlowStore.findBySelector(mkSelectionOptions(primaryEntityRef, "EXACT"));
    let entityCall = primaryEntityRef.kind === 'APPLICATION'
        ? applicationStore.getById(primaryEntityRef.id)
        : actorStore.getById(primaryEntityRef.id);

    let breadcrumbs = [];
    let svgElem;
    let additionalBreadcrumbs = [];
    let entitiesVisited = [];

    $: flowGraphSummary = $flowGraphSummaryCall.data;
    $: physicalFlows = $physicalFlowCall.data;
    $: noOpinionRating = _.find($flowClassificationCall.data, d => d.code === 'NO_OPINION');
    $: entity = $entityCall.data;

    $: flowInfo = _.get(flowGraphSummary?.flowInfoByDirection, [$flowDirection], []);

    $: summarisedFlows = summariseFlows(flowInfo, noOpinionRating);
    $: $categories = mkCategories(summarisedFlows);
    $: $clients = mkClients(summarisedFlows, physicalFlows);
    $: $arcs = mkArcs(summarisedFlows);

    $: select(svgElem)
        .call(zoom()
            .on("zoom", onScroll));

    $: baseBreadcrumb = {
        id: -1,
        name: $flowDirection === flowDirections.INBOUND
            ? `${$focusClient?.name || entity.name} Inbound flows`
            : `${$focusClient?.name || entity.name} Outbound flows`,
        active: true,
        classes: "breadcrumb-root",
        onClick: () => {
            const parent = $focusClient || entity
            flowGraphSummaryCall = logicalFlowStore.getFlowGraphSummary(parent, null, true);
            additionalBreadcrumbs = []
        }
    };

    $: homeBreadcrumb = {
        id: -2,
        name: `Home (${entity.name})`,
        active: true,
        classes: "breadcrumb-home",
        onClick: () => {
            $focusClient = null;
            flowGraphSummaryCall = logicalFlowStore.getFlowGraphSummary(primaryEntityRef, null, true);
            entitiesVisited = [];
            additionalBreadcrumbs = [];
        }
    };


    $: {
        let baseAndDrilldowns = _.concat([baseBreadcrumb], additionalBreadcrumbs);

        breadcrumbs = _
            .chain(baseAndDrilldowns)
            .map(d => Object.assign({}, d, {active: false}))
            .value();

        const lastBreadcrumb = _.last(breadcrumbs);

        lastBreadcrumb.active = true;
        lastBreadcrumb.classes = lastBreadcrumb.classes + " text-muted";
    }

    function onScroll() {
        clientScrollOffset.update(origValue => {
            const dy = event.sourceEvent?.deltaY * -1;
            const minY = _.clamp(
                $clientScale.range()[1] * -1 + 480,
                0);
            return dy
                ? _.clamp(
                    origValue + dy,
                    minY,
                    dimensions.clientList.paddingTop)
                : origValue;
        });
    }


    function loadForCategory(evt) {

        const category = evt.detail;
        const parentEntity = $focusClient || entity;

        flowGraphSummaryCall = logicalFlowStore.getFlowGraphSummary(
            parentEntity,
            category.id,
            true /* force */);

        additionalBreadcrumbs = _.concat(
            additionalBreadcrumbs,
            {
                id: category.id,
                name: category.name,
                active: true,
                onClick: () => {
                    const parent = $focusClient || entity
                    additionalBreadcrumbs = _.dropRightWhile(additionalBreadcrumbs, d => d.id !== category.id);
                    flowGraphSummaryCall = logicalFlowStore.getFlowGraphSummary(parent, category.id, true);
                }
            })
    }

    function selectClient(evt) {

        const newClient = evt.detail;
        const previousClient = $focusClient;

        entitiesVisited = previousClient ? _.concat(entitiesVisited, previousClient) : entitiesVisited;
        additionalBreadcrumbs = [];
        $focusClient = newClient;
        $selectedClient = null;
        flowGraphSummaryCall = logicalFlowStore.getFlowGraphSummary(newClient, null, true);
    }

    function showPrevious() {
        console.log({entitiesVisited});
    }

</script>

<div class="row">
    <div class="col-md-12">
        <ol class="breadcrumb">
            {#if $focusClient}
                <li class={homeBreadcrumb.classes}>
                    <button class="btn btn-skinny"
                            on:click={homeBreadcrumb.onClick}>
                        <Icon size="lg" name="home"/>
                        {homeBreadcrumb.name}
                    </button>
                </li>
            {/if}
            {#if !_.isEmpty(entitiesVisited)}
                <li>
                    <button class="btn btn-skinny"
                            on:click={showPrevious}>
                        ...
                    </button>
                </li>
            {/if}
            {#each breadcrumbs as crumb}
                {#if crumb.active}
                    <li class={crumb.classes}>{crumb.name}</li>
                {:else}
                    <li class={crumb.classes}>
                        <button style="padding: 0"
                                class="btn-skinny"
                                on:click={crumb.onClick}>
                            {crumb.name}
                        </button>
                    </li>
                {/if}
            {/each}
        </ol>
    </div>
</div>


<div class="row row-no-gutters">
    <div class="col-md-12">
        <div class="col-md-7">
            <svg bind:this={svgElem}
                 viewBox={`0 0 ${dimensions.diagram.width} ${dimensions.diagram.height}`}
                 width="100%"
                 height="550"
                 on:click={clearSelections}>

                <clipPath id="row-clip">
                    <rect x="0"
                          y="0"
                          width={dimensions.client.width}
                          height={dimensions.diagram.height}/>
                </clipPath>

                <g id="categories"
                   transform={`translate(${$layout.categoryTranslateX}, 0)`}>
                    <Categories on:select={loadForCategory}/>
                </g>

                <g id="clients"
                   clip-path="url(#row-clip)"
                   transform={`translate(${$layout.clientTranslateX}, 0)`}>
                    <Clients/>
                </g>

                <g id="arcs">
                    <Arcs/>
                </g>
            </svg>
            {#if _.size(summarisedFlows) === 0 }
                <NoData>
                    <Icon name="exclamation-triangle"/>
                    No flows can be found for this set of filters. Try toggling the direction or navigating up the tree to view more flows.
                </NoData>
            {/if}
        </div>
        <div class="col-md-5" style="padding-left: 1em">
            <FlowContextPanel parentEntity={entity}
                              {flowInfo}
                              on:select={selectClient}/>
        </div>
    </div>
</div>

<style>
    svg {
        margin: 10px;
        padding: 6px;
        border: 1px solid #eee;
    }

    .breadcrumb {
        margin-bottom: 0.4em;
    }

    /* this is used */
    .breadcrumb-root {
        font-weight: bold;
    }

    .breadcrumb-home button {
        font-weight: bold !important;
    }
</style>