<script>
    import {platformStrategy, afcGovernance, situationalAppraisal, singlePayments, dailyRisk} from "./demo-data";
    import {calcBounds, calcViewBox, mkLayoutData} from "./process-diagram-utils";
    import Defs from "./Defs.svelte";
    import Objects from "./Objects.svelte";
    import Connections from "./Connections.svelte";
    import {scaleLinear} from "d3-scale";
    import {zoom} from "d3-zoom";
    import {event, select} from "d3-selection";

    const process = singlePayments;

    let viewBox = calcViewBox(process.positions);
    let bounds = calcBounds(process.positions);

    const y = scaleLinear()
        .domain([bounds.y1, bounds.y2])
        .range([bounds.y2, bounds.y1]);

    const layoutDataById = mkLayoutData(process.objects, process.positions);

    const connections = _
        .chain(process.connections)
        .reject(conn => conn.hidden)
        .filter(conn => layoutDataById[conn.startObjectId] && layoutDataById[conn.endObjectId])
        .value(); // ditch conns w/o objs

    // pan + zoom
    function zoomed() {
        const t = event.transform;
        svgElem.attr("transform", t);
    }

    let elem;
    $: svgElem = select(elem);

    $: svgElem.call(zoom().on("zoom", zoomed));

</script>

<svg width="100%"
     height="700"
     preserveAspectRatio="xMinYMin"
     {viewBox}>

    <Defs/>

    <g transform=""
       bind:this={elem}>
        <Connections {connections}
                     {layoutDataById}/>

        <Objects objects={process.objects}
                 {layoutDataById}/>
    </g>

</svg>

<style>
    svg {
        border: 1px solid red;
    }
</style>
