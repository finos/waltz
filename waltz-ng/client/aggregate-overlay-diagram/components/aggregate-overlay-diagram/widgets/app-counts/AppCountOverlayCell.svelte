<script>
    import {scaleLinear} from "d3-scale";

    export let cellData = {};
    export let maxCount;

    $:r = scaleLinear()
        .domain([0, maxCount])
        .range([0, 45]);

    $: cr = r(cellData?.currentStateCount) || 0;
    $: tr = r(cellData?.targetStateCount) || 0;
</script>


<svg class="content" viewBox="0 0 300 100">

    <circle r={cr}
            fill="#b2ffca"
            stroke="#31ff89"
            stroke-width="2"
            cx={150 - (cr / 1.6)}
            cy="50"/>
    <circle r={tr}
            fill="#90ffab"
            stroke="#31ff89"
            stroke-width="2"
            cx={150 + (cr / 1.6)}
            cy="50"/>

    <foreignObject transform="translate(15, 5)"
                   width="270"
                   height="90">
        {#if cellData}
            <div style="font-size: 22px; width: 100%; text-align: center">
                App Count: #{cellData?.currentStateCount} &raquo; #{cellData?.targetStateCount}
            </div>
            <div style="font-size: 16px; width: 100%; text-align: center">
                Change: {cellData?.targetStateCount - cellData?.currentStateCount}
            </div>
        {:else}
            -
        {/if}
    </foreignObject>
</svg>
