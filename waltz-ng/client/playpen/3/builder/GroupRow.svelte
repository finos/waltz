<script>

    import EntityCell from "./EntityCell.svelte";

    export let row;
    export let dimensions;
    export let color;
    export let cellHeight;
    export let statsBoxHeight;
    export let statsBoxWidth;

    const rowWidth = dimensions.w - dimensions.labelWidth;

    const cellWidth = rowWidth / row.length;
</script>


{#each row as cell, idx}
    <g transform={`translate(${idx * cellWidth})`}
       class="data-cell"
       data-cell-id={cell.id}
       data-cell-name={cell.name}>
        <EntityCell {cell}
                    {cellHeight}
                    {statsBoxHeight}
                    {statsBoxWidth}
                    {cellWidth}
                    {dimensions}
                    {color}/>
    </g>
{/each}
