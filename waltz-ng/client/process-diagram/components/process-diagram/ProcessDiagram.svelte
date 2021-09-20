<script>
    import {calcBounds, calcViewBox} from "./process-diagram-utils";
    import Defs from "./Defs.svelte";
    import Objects from "./Objects.svelte";
    import Connections from "./Connections.svelte";
    import {scaleLinear} from "d3-scale";
    import {zoom} from "d3-zoom";
    import {event, select} from "d3-selection";
    import {positions, initData} from "./diagram-store";

    // pan + zoom
    function zoomed() {
        const t = event.transform;
        select(elem).select("g").attr("transform", t);
    }

    let elem;

    $: viewBox = calcViewBox($positions);
    $: bounds = calcBounds($positions);

    $: y = scaleLinear()
        .domain([bounds.y1, bounds.y2])
        .range([bounds.y2, bounds.y1]);

    $: svgElem = select(elem);
    $: svgElem.call(zoom().on("zoom", zoomed));
</script>


<svg bind:this={elem}
     width="100%"
     height="700"
     preserveAspectRatio="xMinYMin"
     {viewBox}>

    <Defs/>

    <g>
        <Connections/>
        <Objects/>
    </g>
</svg>


<style>
    svg {
        border: 1px solid #eee;
    }
</style>
