<script>
    export let connections = [];

</script>

<g class="connections">
    {#each connections as conn}
        <line x1={conn.startObject.bottomRight.x}
              y1={conn.startObject.topLeft.y + conn.startObject.height / 2}
              x2={conn.endObject.topLeft.x}
              y2={conn.endObject.topLeft.y + conn.endObject.height / 2}>
        </line>
    {/each}
</g>

<style>
    line {
        stroke: red;
        stroke-width: 3;
    }
</style>
