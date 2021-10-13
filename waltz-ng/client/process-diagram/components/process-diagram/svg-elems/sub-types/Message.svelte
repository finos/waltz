<script>
    export let width = 10;
    export let height= 10;

    $: envelope = `
        M ${width/4}, ${height/3}
        L ${width/4}, ${height/3 * 2}
        L ${width/4 * 3}, ${height/3 * 2}
        L ${width/4 * 3}, ${height/3}
        L ${width/4}, ${height/3}
    `;
    $: lid = `
        M ${width/4}, ${height/3}
        L ${width/2}, ${height/2}
        L ${width/4 * 3}, ${height/3}
    `;
</script>

<path stroke-linecap="square"
      d={envelope}>
</path>

<path fill="none"
      d={lid}>
</path>
