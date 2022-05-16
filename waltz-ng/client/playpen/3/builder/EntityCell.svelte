<script>

    import StatisticsBox from "./StatisticsBox.svelte";
    import CalloutBox from "./CalloutBox.svelte";

    export let cell;
    export let cellWidth;
    export let dimensions;
    export let color;

    let statBoxWidth = 0;

    $: statBoxWidth = dimensions.cell.statsHeight * 3;

</script>


<g class="outer">
    <rect stroke="white"
          stroke-width="5"
          width={cellWidth}
          height={dimensions.cell.height}
          fill={color}>
    </rect>
    <CalloutBox width={dimensions.callout.width}
                height={dimensions.callout.height}/>
</g>


<g transform="translate(0, 10)">
    <foreignObject transform={`translate(${dimensions.cell.height * 0.25} 0)`}
                   width={cellWidth - dimensions.cell.height * 0.5}
                   height={dimensions.cell.labelHeight}>
        <div class="cell-title">
            {cell.name}
        </div>
    </foreignObject>
    <g transform={`translate(${cellWidth / 2 - (statBoxWidth / 2)}, ${dimensions.cell.labelHeight})`}>
        <StatisticsBox width={statBoxWidth}
                       height={dimensions.cell.statsHeight}>
        </StatisticsBox>
    </g>
</g>


<style>

    .cell-title {
        height: 100%;
        text-align: center;
        color: black;
    }
</style>