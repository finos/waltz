<script>
    export let connections = [];
    export let layoutDataById = {};

</script>

<g class="connections">
    {#each connections as conn}
        <line x1={layoutDataById[conn.startObjectId].x + layoutDataById[conn.startObjectId].width }
              y1={layoutDataById[conn.startObjectId].y + layoutDataById[conn.startObjectId].height / 2}
              x2={layoutDataById[conn.endObjectId].x}
              y2={layoutDataById[conn.endObjectId].y + layoutDataById[conn.endObjectId].height / 2}>
        </line>
    {/each}
</g>

<style>
    line {
        stroke: red;
        stroke-width: 3;
    }
</style>
