<script>

    import EntityCell from "./EntityCell.svelte";

    export let row;
    export let dimensions;
    export let color;

    const rowWidth = dimensions.w - dimensions.labelWidth;


    const cellWidth = rowWidth / row.length;
</script>


<g>
    {#each row as cell, idx}
        <g transform={`translate(${idx * cellWidth})`}
           class="data-cell"
           data-cell-id={cell.id}>
            <EntityCell {cell} {cellWidth} {dimensions} {color}/>
        </g>
    {/each}
</g>