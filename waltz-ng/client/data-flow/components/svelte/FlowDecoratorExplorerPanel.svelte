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
        selectedClient,
        parentCategory,
        startingCategory,
        layoutDirection,
        layoutDirections,
        filterApplied,
        clientQuery,
        categoryQuery,
        entityKindFilter,
        assessmentRatingFilter,
        selectedRating
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
    import {sameRef} from "../../../common/entity-utils";

    export let primaryEntityRef;

    let flowGraphSummaryCall = logicalFlowStore.getFlowGraphSummary(primaryEntityRef, null);
    let flowClassificationCall = flowClassificationStore.findAll()
    let physicalFlowCall = physicalFlowStore.findBySelector(mkSelectionOptions(primaryEntityRef, "EXACT"));
    let entityCall = primaryEntityRef.kind === 'APPLICATION'
        ? applicationStore.getById(primaryEntityRef.id)
        : actorStore.getById(primaryEntityRef.id);

    let breadcrumbs = [];
    let additionalBreadcrumbs = [];
    let entitiesVisited = [];
    let svgElem;

    $: flowGraphSummary = $flowGraphSummaryCall.data;
    $: physicalFlows = $physicalFlowCall.data;
    $: noOpinionRating = _.find($flowClassificationCall.data, d => d.code === 'NO_OPINION');
    $: entity = $entityCall.data;

    $: flowInfo = _.get(flowGraphSummary?.flowInfoByDirection, [$flowDirection], []);
    $: $startingCategory = flowGraphSummary?.startingDataType;
    $: $parentCategory = flowGraphSummary?.parentDataType;

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
            category?.id,
            false /* no force */);

        if (category == null) {
            additionalBreadcrumbs = [];
        } else {
            const matchingBreadcrumbFn = b => b.id === category?.id && sameRef(parentEntity, b.parentEntity);

            const dropBreadcrumbs = () => {
                return _.dropRightWhile(additionalBreadcrumbs, d => !matchingBreadcrumbFn(d))
            }

            const addBreadcrumb = () => {
                return [
                    ...additionalBreadcrumbs,
                    {
                        parentEntity,
                        id: category.id,
                        name: category.name,
                        onClick: () => loadForCategory({detail: category})
                    }
                ];
            };

            additionalBreadcrumbs = _.find(additionalBreadcrumbs, matchingBreadcrumbFn)
                ? dropBreadcrumbs()
                : addBreadcrumb();
        }
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

    function toggleDirection() {
        $layoutDirection = ($layoutDirection === layoutDirections.categoryToClient)
            ? layoutDirections.clientToCategory
            : layoutDirections.categoryToClient
    }

    function clearFilters() {
        $clientQuery = null;
        $categoryQuery = null;
        $entityKindFilter = () => true;
        $assessmentRatingFilter = () => true;
        $selectedRating = null;
    }

</script>

<div class="row">
    <div class="col-md-12">
        <ol class="breadcrumb">
            <li>
                <button title={$filterApplied ? "Click to clear filters" : "You can filter using the 'Filters' tab"}
                        class="btn btn-skinny"
                        class:filters-active={$filterApplied}
                        class:filters-disabled={!$filterApplied}
                        disabled={!$filterApplied}
                        on:click={() => clearFilters()}>
                    <Icon size="lg"
                          name="filter"/>
                </button>
                <button title="toggle flow direction"
                        class="btn btn-skinny"
                        on:click={toggleDirection}>
                    <Icon size="lg" name="exchange"/>
                </button>
            </li>
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
                 on:click={clearSelections}
                 on:keydown={clearSelections}>

                <clipPath id="row-clip">
                    <rect x="0"
                          y="0"
                          width={dimensions.client.width}
                          height={dimensions.diagram.height}/>
                </clipPath>

                <g id="categories"
                   transform={`translate(${$layout.categoryTranslateX}, 0)`}>
                    <Categories kind={$focusClient?.kind || primaryEntityRef.kind}
                                on:select={loadForCategory}/>
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

    .filters-active {
        color: red;
    }

    .filters-disabled {
        color: darkgrey;
    }


</style>