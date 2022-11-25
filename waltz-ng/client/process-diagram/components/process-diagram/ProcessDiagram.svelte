<script>
    import {calcBounds, calcViewBox, clearSelections} from "./process-diagram-utils";
    import Defs from "./svg-elems/Defs.svelte";
    import Objects from "./svg-elems/Objects.svelte";
    import Connections from "./svg-elems/Connections.svelte";
    import {zoom} from "d3-zoom";
    import {event, select} from "d3-selection";
    import {diagramInfo, positions} from "./diagram-store";
    import ProcessDiagramContextPanel from "./context-panels/ProcessDiagramContextPanel.svelte";
    import ImageDownloadLink from "../../../common/svelte/ImageDownloadLink.svelte";

    // pan + zoom
    function zoomed() {
        const t = event.transform;
        select(elem).select("g").attr("transform", t);
    }

    let elem;

    $: viewBox = calcViewBox($positions);
    $: bounds = calcBounds($positions);

    $: svgElem = select(elem);
    $: svgElem.call(zoom().on("zoom", zoomed));


</script>


<div class="row row-no-gutters">
    <div class="col-md-9">
        <svg bind:this={elem}
             width="100%"
             height="700"
             preserveAspectRatio="xMinYMin"
             on:click={clearSelections}
             on:keydown={clearSelections}
             {viewBox}>

            <Defs/>

            <g>
                <Connections/>
                <Objects/>
            </g>
        </svg>
        <ImageDownloadLink styling="link"
                           element={elem?.parentElement}
                           filename={`${$diagramInfo?.name}-process-diagram.png`}/>
    </div>
    <div class="col-md-3"
         style="padding-left: 1em">
        <ProcessDiagramContextPanel/>
    </div>
</div>


<style>
    svg {
        border: 1px solid #eee;
    }
</style>
