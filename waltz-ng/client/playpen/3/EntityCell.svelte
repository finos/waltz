<script>

    import StatisticsBox from "./StatisticsBox.svelte";

    export let cell;
    export let cellWidth;
    export let dimensions;
    export let color;

</script>


<rect stroke="white"
      stroke-width="5"
      width={cellWidth}
      height={dimensions.cell.height}
      fill={color}>
</rect>

<g transform="translate(0, 10)">
    <foreignObject width={cellWidth}
                   height={dimensions.cell.labelHeight}>
        <div class="cell-title">
            {cell.name}
        </div>
    </foreignObject>
    <g transform={`translate(${cellWidth / 2 - (cellWidth / 1.2 / 2)}, ${dimensions.cell.labelHeight})`}>
        <StatisticsBox width={cellWidth / 1.2}
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