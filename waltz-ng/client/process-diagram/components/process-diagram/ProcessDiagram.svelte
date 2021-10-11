<script>
    import {calcBounds, calcViewBox} from "./process-diagram-utils";
    import Defs from "./svg-elems/Defs.svelte";
    import Objects from "./svg-elems/Objects.svelte";
    import Connections from "./svg-elems/Connections.svelte";
    import {scaleLinear} from "d3-scale";
    import {zoom} from "d3-zoom";
    import {event, select} from "d3-selection";
    import {positions} from "./diagram-store";
    import ProcessDiagramContextPanel from "./context-panels/ProcessDiagramContextPanel.svelte";

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


<div class="row">
    <div class="col-md-9">
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

    </div>
    <div class="col-md-3">
        <ProcessDiagramContextPanel/>
    </div>
</div>


<style>
    svg {
        border: 1px solid #eee;
    }
</style>
