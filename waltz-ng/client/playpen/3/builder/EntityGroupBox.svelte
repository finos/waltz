<script>

    import GroupRow from "./GroupRow.svelte";
    import {calcHeight} from "./overlay-diagram-builder-utils";

    export let dimensions;
    export let group;
    export let height = 200;
    export let color;
</script>

{#if dimensions}
<g class="entity-group-box"
   data-group-id={group.id}>

    <g class="outer">
        <rect width={dimensions.w}
              {height}>
        </rect>
        <foreignObject width={dimensions.labelWidth}
                       height={height}>
            <div class="group-title">
                {group.name}
            </div>
        </foreignObject>
    </g>

    <g transform={`translate(${dimensions.labelWidth})`}>
        {#each group.rows as row, idx}
            <g transform={`translate(0, ${calcHeight(idx, dimensions)})`}>
                <GroupRow {row} {dimensions} {color}/>
            </g>
        {/each}
    </g>
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
        background-color: #0e2541;
        height: 100%;
        padding: 0.5em;
        color: #f2f6f2;
        border: solid 2px white;
        display: -webkit-flex;
        display: flex;
        align-items: center;
    }
</style>
