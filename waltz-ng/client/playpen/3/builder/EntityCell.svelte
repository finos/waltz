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


<!-- CELL BACKGROUND -->
<rect transform="translate(4 4)"
      width={cellWidth-8}
      height={dimensions.cell.height-8}
      class="cell-background"
      fill={color}>
</rect>

<path d={`M ${cellWidth-34} 4 l30 0 l0 30 Z`}
      class="cell-related-entity-indicator"
      fill="#1B96FF">
    <title>This cell is directly related to the parent entity</title>
</path>

<CalloutBox width={dimensions.callout.width}
            height={dimensions.callout.height}/>

<g transform="translate(0, 10)">
    <!-- CELL TITLE -->
    <foreignObject transform={`translate(${dimensions.cell.height * 0.25} 0)`}
                   width={cellWidth - dimensions.cell.height * 0.5}
                   height={dimensions.cell.labelHeight}>
        <div class="cell-title">
            {cell.name}
        </div>
    </foreignObject>

    <!-- STATS BOX -->
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