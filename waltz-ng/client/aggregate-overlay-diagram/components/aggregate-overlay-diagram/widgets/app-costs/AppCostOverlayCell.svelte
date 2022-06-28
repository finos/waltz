<script>
    import {numberFormatter} from "../../../../../common/string-utils";
    import {scaleLinear} from "d3-scale";

    export let cellData = {};
    export let maxCost;

    $:r = scaleLinear()
        .domain([0, maxCost])
        .range([0, 45]);

    $: tr = r(cellData?.totalCost) || 0;

    $: console.log("overlay cell", {maxCost});
</script>


<svg class="content"
     viewBox="0 0 300 100">
    <circle r={tr}
            fill="#c6eeff"
            stroke="#25b0ff"
            stroke-width="2"
            cx={250}
            cy="50"/>
    <foreignObject transform="translate(15, 5)"
                   width="270"
                   height="90">
        {#if cellData?.appCount}
            <div style="font-size: 26px; width: 100%;">
                App Costs: <strong>{numberFormatter(cellData?.totalCost, 2)}</strong>
                <br>
            </div>
            <div style="font-size: 22px; width: 100%;">
                App Count: {cellData?.appCount}
            </div>
        {:else}
            -
        {/if}
    </foreignObject>
</svg>
