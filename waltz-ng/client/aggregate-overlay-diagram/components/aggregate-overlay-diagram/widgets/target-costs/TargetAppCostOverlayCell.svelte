<script>
    import {numberFormatter} from "../../../../../common/string-utils";
    import {scaleLinear} from "d3-scale";
    import _ from "lodash";

    export let cellData = null;
    export let maxCost;

    $: r = scaleLinear()
        .domain([0, maxCost])
        .range([0, 45]);

    $: cr = r(cellData?.currentStateCost) || 0;
    $: tr = r(cellData?.targetStateCost) || 0;
    $: delta = cellData?.currentStateCost - cellData?.targetStateCost;
</script>


<svg class="content" viewBox="0 0 300 100">
    <circle r={cr}
            fill="#a9e4ff"
            stroke="#25b0ff"
            stroke-width="2"
            cx={150 - (cr / 1.4)}
            cy="50"/>

    {#if (delta !== 0)}
        <circle r={tr}
                fill="#c6eeff"
                stroke="#25b0ff"
                stroke-width="2"
                cx={150 + (cr / 1.4)}
                cy="50"/>
    {/if}
    <foreignObject transform="translate(15, 5)"
                   width="270"
                   height="90">
        {#if _.isNil(cellData)}
            -
        {:else}
            <div style="font-size: 22px; width: 100%; text-align: center">
                App Costs:
                <br>
                {numberFormatter(cellData?.currentStateCost, 2)} &raquo; {numberFormatter(cellData?.targetStateCost, 2)}
            </div>
            <div style="font-size: 16px; width: 100%; text-align: center">
                Change: {numberFormatter(delta, 2)}
            </div>
        {/if}
    </foreignObject>
</svg>
