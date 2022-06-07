<script>
    import {scaleLinear} from "d3-scale";
    import _ from "lodash";

    export let cellData = null;
    export let maxCount = 0;
    export let height;
    export let width;

    $: r = scaleLinear()
        .domain([0, maxCount])
        .range([0, height - 8])
        .clamp(true);

    $: cr = r(cellData?.currentStateCount) || 0;
    $: tr = r(cellData?.targetStateCount) || 0;

    $: console.log({height, width, maxCount, current: cellData?.currentStateCount, target: cellData?.targetStateCount});
</script>


<svg class="content"
     viewBox={`0 0 ${width} ${height}`}>

    <circle r={cr}
            fill="#b2ffca"
            stroke="#31ff89"
            stroke-width="2"
            cx={width / 2 - (cr / 1.6)}
            cy={height / 2}/>
    <circle r={tr}
            fill="#90ffab"
            stroke="#31ff89"
            stroke-width="1"
            cx={width / 2 + (tr / 1.6)}
            cy={height / 2}/>

    <foreignObject transform={`translate(${width / 8}, ${height / 4})`}
                   width={width * 0.75}
                   height={height / 2}>
        {#if _.isNil(cellData)}
            -
        {:else}
            <div style={`font-size: ${height/5}px; width: 100%; text-align: center`}>
                App Count: #{cellData?.currentStateCount} &raquo; #{cellData?.targetStateCount}
            </div>
            <div style={`font-size: ${height / 6}px; width: 100%; text-align: center`}>
                Change: {cellData?.targetStateCount - cellData?.currentStateCount}
            </div>
        {/if}
    </foreignObject>
</svg>
