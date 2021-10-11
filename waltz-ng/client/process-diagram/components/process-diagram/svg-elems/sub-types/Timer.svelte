<script>
    export let width = 10;
    export let height= 10;


    $: hands = `
        M ${width/5 * 2}, ${height/5 * 2}
        L ${width/2}, ${height/2}
        L ${width/7 * 5}, ${height/2}
    `;
</script>

<circle cx={width / 2}
        cy={height / 2}
        r={width/4}
        fill="none">
</circle>

<path fill="none"
      d={hands}>
</path>
