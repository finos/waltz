<script>

    import GroupRow from "./GroupRow.svelte";
    import {calcHeight} from "./overlay-diagram-utils";

    export let dimensions;
    export let group;
    export let height = 200;
    export let color;

    $: console.log({color, group})
</script>

{#if dimensions}
<g class="entity-group-box">
    <rect class="outer"
          width={dimensions.w - (dimensions.padding + dimensions.group.padding) * 2}
          {height}>
    </rect>


    <foreignObject width={dimensions.labelWidth}
                   height={height}>
        <div class="group-title">
            {group.name}
        </div>
    </foreignObject>

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
    rect.outer {
        stroke: #fff;
        stroke-width: 2;
    }

    .group-title {
        font-weight: bolder;
        background-color: #0e2541;
        height: 100%;
        padding: 1em;
        color: #f2f6f2;
    }
</style>
