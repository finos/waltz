<script>
    import {numberFormatter} from "../../../../../common/string-utils";
    import {scaleLinear} from "d3-scale";
    import _ from "lodash";

    export let cellData = null;
    export let maxCost;
    export let height;
    export let width;

    $: console.log({maxCost});

    $: r = scaleLinear()
        .domain([0, maxCost])
        .range([0, height / 2 - 2]);

    $: cr = r(cellData?.currentStateCost) || 0;
    $: tr = r(cellData?.targetStateCost) || 0;
    $: delta = cellData?.currentStateCost - cellData?.targetStateCost;

</script>


<svg class="content"
     viewBox={`0 0 ${width} ${height}`}>
    <circle r={cr}
            fill="#a9e4ff"
            stroke="#25b0ff"
            stroke-width="1"
            cx={width / 2 - (cr / 1.4)}
            cy={height / 2}/>

    {#if (delta !== 0)}
        <circle r={tr}
                fill="#c6eeff"
                stroke="#25b0ff"
                stroke-width="1"
                cx={width / 2 + (cr / 1.4)}
                cy={height / 2}/>
    {/if}
    <foreignObject transform={`translate(${width / 8}, ${height / 4})`}
                   width={width * 0.75}
                   height={height / 2}>
        {#if _.isNil(cellData)}
            -
        {:else}
            <div style={`font-size: ${height / 5}px; width: 100%; text-align: center`}>
                App Costs:
                <br>
                {numberFormatter(cellData?.currentStateCost, 2)} &raquo; {numberFormatter(cellData?.targetStateCost, 2)}
            </div>
            <div style={`font-size: ${height / 5}px; width: 100%; text-align: center`}>
                Change: {numberFormatter(delta, 2)}
            </div>
        {/if}
    </foreignObject>
</svg>


<style>

    svg {
        display: block;
    }

</style>