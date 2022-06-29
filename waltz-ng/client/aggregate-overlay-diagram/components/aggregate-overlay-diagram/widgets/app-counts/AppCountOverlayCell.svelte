<script>
    import {scaleLinear} from "d3-scale";
    import _ from "lodash";

    export let cellData = null;
    export let maxCount = 0;
    export let height;
    export let width;

    $: r = scaleLinear()
        .domain([0, maxCount])
        .range([2, height / 2 - 2])
        .clamp(true);

    $: cr = r(cellData?.currentStateCount) || 0;
    $: tr = r(cellData?.targetStateCount) || 0;

</script>


<svg class="content"
     viewBox={`0 0 ${width} ${height}`}>

    <circle r={cr}
            fill="#b2ffca"
            stroke="#31ff89"
            stroke-width="1"
            cx={width / 2 - (cr / 1.6)}
            cy={height / 2}/>
    <circle r={tr}
            fill="#90ffab"
            stroke="#31ff89"
            stroke-width="1"
            cx={width / 2 + (tr / 1.6)}
            cy={height / 2}/>

    <text x={width * 0.5}
          text-anchor="middle"
          font-size={height * 0.4}
          y={height * 0.45}>
        {#if !_.isNil(cellData)}
            App Count: #{cellData?.currentStateCount} &raquo; #{cellData?.targetStateCount}
        {:else}
            -
        {/if}
    </text>

    <text x={width * 0.5}
          text-anchor="middle"
          font-size={height * 0.2}
          y={height * 0.85}>
        {#if !_.isNil(cellData) && cellData?.targetStateCount !== cellData?.currentStateCount}
            Change: {cellData?.targetStateCount - cellData?.currentStateCount}
        {/if}
    </text>

<!--    <foreignObject transform={`translate(${width / 8}, ${height / 4})`}-->
<!--                   width={width * 0.75}-->
<!--                   height={height / 2}>-->
<!--        {#if _.isNil(cellData)}-->
<!--            - -->
<!--        {:else}-->
<!--            <div style={`font-size: ${height/4}px; width: 100%; text-align: center`}>-->
<!--                App Count: #{cellData?.currentStateCount} &raquo; #{cellData?.targetStateCount}-->
<!--            </div>-->
<!--            <div style={`font-size: ${height / 5}px; width: 100%; text-align: center`}>-->
<!--                Change: {cellData?.targetStateCount - cellData?.currentStateCount}-->
<!--            </div>-->
<!--        {/if}-->
<!--    </foreignObject>-->
</svg>
