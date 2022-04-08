<script>

    import EntityCell from "./EntityCell.svelte";

    export let row;
    export let dimensions;
    export let color;

    const rowWidth = dimensions.w - dimensions.labelWidth;


    const cellWidth = rowWidth / row.length;


    $: console.log({row, cellWidth});
</script>


<g>
    {#each row as cell, idx}
        <g transform={`translate(${idx * cellWidth})`}>
            <EntityCell {cell} {cellWidth} {dimensions} {color}/>
        </g>
    {/each}
</g>