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
    <rect class="outer"
          stroke="#fff"
          stroke-width="2"
          width={dimensions.w - (dimensions.padding + dimensions.group.padding) * 2}
          {height}>
    </rect>

    <foreignObject width={dimensions.labelWidth}
                   height={height}>
        <div class="group-title"
             style={`
                background-color: ${group.headerColor || '#0e2541'};
                font-weight: bolder;
                height: 100%;
                padding: 1em;
                color: #f2f6f2;`}>
            {group.name}
        </div>
    </foreignObject>

    <g transform={`translate(${dimensions.labelWidth})`}>
        {#each group.rows as row, idx}
            <g transform={`translate(0, ${calcHeight(idx, dimensions)})`}>
                <GroupRow {row}
                          {dimensions}
                          color={group.cellColor || color}/>
            </g>
        {/each}
    </g>
</g>
{/if}