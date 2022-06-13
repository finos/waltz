<script>
    import GroupRow from "./GroupRow.svelte";
    import {calcHeight} from "./overlay-diagram-builder-utils";
    import CalloutBox from "./CalloutBox.svelte";

    export let dimensions;
    export let group;
    export let layoutData = {height: 200};
    export let color;

    $: cellHeight = group.cellHeight || dimensions?.cell?.height;
    $: statsHeight = group.statsBoxHeight || cellHeight / 3;
    $: statsWidth = group.statsBoxWidth || dimensions?.cell?.statsBoxWidth || statsHeight * 3;

</script>

{#if dimensions}
<g class="entity-group-box outer"
   transform={`translate(0 ${layoutData.dy})`}
   data-cell-id={group.id}
   data-cell-name={group.name}>

    <!-- GROUP HEADER -->
    <rect width={dimensions.w}
          height={layoutData.height}
          fill="none">
    </rect>
    <rect width={dimensions.labelWidth}
          height={layoutData.height}
          class="section-header cell-background"
          fill={group.headerColor || '#0e2541'}>
    </rect>
    <foreignObject width={dimensions.labelWidth}
                   height={layoutData.height}>
        <div class="group-title">
            {group.name}
        </div>
    </foreignObject>

    <!-- GROUP CALLOUT -->
    <CalloutBox width={dimensions.callout.width}
                height={dimensions.callout.height}/>

    {#each group.rows as row, idx}
        <!-- ROW -->
        <g transform={`translate(${dimensions.labelWidth}, ${idx * (group.cellHeight || dimensions.cell.height)})`}>
            <GroupRow {row}
                      {dimensions}
                      color={group.cellColor || color}
                      {cellHeight}
                      statsBoxHeight={statsHeight}
                      statsBoxWidth={statsWidth}/>
        </g>
    {/each}
</g>
{/if}


<style>
    .outer rect {
        stroke: #fff;
        stroke-width: 2;
        background-color: white;
    }

    .group-title {
        font-weight: bolder;
        height: 100%;
        padding: 0.5em;
        color: #f2f6f2;
        border: solid 2px white;
        display: -webkit-flex;
        display: flex;
        align-items: center;
    }
</style>
