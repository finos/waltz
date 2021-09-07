
<script>
    import {
        arcs,
        categories,
        categoryQry,
        clientQry,
        clients,
        clientScale,
        clientScrollOffset,
        layout,
        layoutDirection,
        layoutDirections
    } from "./scroll-store";
    import Categories from "./Categories.svelte";
    import Clients from "./Clients.svelte";
    import {dimensions} from "./scroll-utils";
    import {mkArcs, mkCategories, mkClients} from "./demo-data";
    import _ from "lodash";
    import Arcs from "./Arcs.svelte";
    import {select, event} from "d3-selection";
    import {zoom} from "d3-zoom";
    import {logicalFlowStore} from "../../../svelte-stores/logical-flow-store";

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

    export let primaryEntityRef;

    let flowGraphSummaryCall = logicalFlowStore.getFlowGraphSummary(primaryEntityRef, null);

    $: flowGraphSummary = $flowGraphSummaryCall.data;

    $: console.log({primaryEntityRef, flowGraphSummary});

    let svgElem;
    let directionToggle = false;

    $: $categories = mkCategories(flowInfo);
    $: $clients = mkClients(flowInfo);
    $: $arcs = mkArcs(summarisedFlows);

    $: select(svgElem)
        .call(zoom()
            .on("zoom", onScroll));

    $: $layoutDirection = directionToggle
        ? layoutDirections.clientToCategory
        : layoutDirections.categoryToClient


    $: flowInfo = _.get(flowGraphSummary?.flowInfoByDirection, [$layoutDirection === layoutDirections.categoryToClient ? "OUTBOUND" : "INBOUND"], []);

    $: groupedFlowInfo = _
        .chain(flowInfo)
        .map(d => Object.assign({}, d, { key: `cat_${d.rollupDataType.id}_cli_${d.counterpart.id}`}))
        .groupBy(d => d.key)
        .value()

    $: summarisedFlows = _
        .chain(groupedFlowInfo)
        .mapValues((v, k) => {

            const flow = _.head(v);

            const ratingCounts = _
                .chain(v)
                .filter(v => v.actualDataType.id !== v.rollupDataType.id)
                .countBy(v => v.classificationId)
                .value();

            const exactFlow = _.find(v, d => d.actualDataType.id === d.rollupDataType.id);

            const lineRating = _.get(exactFlow, "classificationId", 2);

            const lineLifecycleStatus = _.get(exactFlow, "flowEntityLifecycleStatus", "ACTIVE");

            return {
                key: k,
                ratings: v,
                hasChildren: _.isEmpty(ratingCounts),
                ratingCounts,
                lineRating,
                lineLifecycleStatus,
                categoryId: flow.rollupDataType.id,
                clientId: flow.counterpart.id
            };
        })
        .value()


    $: console.log({groupedFlowInfo, summarisedFlows});


</script>

<div>
    <label for="toggle-direction">
        Toggle direction:
        <input id="toggle-direction"
               type="checkbox"
               bind:checked={directionToggle}>
    </label>
    Filter categories: <input type="text" bind:value={$categoryQry}/>
    Filter clients: <input type="text" bind:value={$clientQry}/>
</div>

<div>
    <svg bind:this={svgElem}
         viewBox={`0 0 ${dimensions.diagram.width} ${dimensions.diagram.height}`}
         width="700"
         height="500">

        <clipPath id="row-clip">
            <rect x="0"
                  y="0"
                  width={dimensions.client.width}
                  height={dimensions.diagram.height}/>
        </clipPath>

        <g id="categories"
           transform={`translate(${$layout.categoryTranslateX}, 0)`}>
            <Categories/>
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
</div>

<style>
    svg {
        margin: 10px;
        padding: 6px;
        border: 1px solid #eee;
    }
</style>