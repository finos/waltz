<script>

    import StatisticsBox from "./StatisticsBox.svelte";

    export let cell;
    export let cellWidth;
    export let dimensions;
    export let color;

    let statBoxWidth = 0;

    $: statBoxWidth = dimensions.cell.statsHeight * 3;

</script>


<rect stroke="white"
      stroke-width="5"
      width={cellWidth}
      height={dimensions.cell.height}
      fill={color}>
</rect>

<g transform="translate(0, 10)"
   data-cell-id={cell.id}>
    <foreignObject width={cellWidth}
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