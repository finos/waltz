<script>

    import StatisticsBox from "./StatisticsBox.svelte";
    import CalloutBox from "./CalloutBox.svelte";

    export let cell;
    export let cellWidth;
    export let cellHeight;
    export let statsBoxHeight;
    export let statsBoxWidth;
    export let dimensions;
    export let color;

</script>


<!-- CELL BACKGROUND -->
<rect transform="translate(4 4)"
      width={cellWidth - 8}
      height={cellHeight - 8}
      class="cell-background"
      fill={color}>
</rect>

<path d={`M ${cellWidth - 34} 4 l30 0 l0 30 Z`}
      class="cell-related-entity-indicator"
      fill="#1B96FF">
    <title>This cell is directly related to the parent entity</title>
</path>

<CalloutBox width={dimensions.callout.width}
            height={dimensions.callout.height}/>

<g transform="translate(0, 10)">
    <!-- CELL TITLE -->
    <foreignObject transform={`translate(${cellHeight * 0.25} 0)`}
                   width={cellWidth - cellHeight * 0.5}
                   height={dimensions.cell.labelHeight}>
        <div class="cell-title">
            {cell.name}
        </div>
    </foreignObject>

    <!-- STATS BOX -->
    <g transform={`translate(${cellWidth / 2 - (statsBoxWidth / 2)}, ${(cellHeight * 0.75 - dimensions.cell.labelHeight) / 2 - statsBoxHeight / 2 + dimensions.cell.labelHeight})`}>
        <StatisticsBox width={statsBoxWidth}
                       height={statsBoxHeight}>
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