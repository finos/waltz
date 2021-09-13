<script>
    import {mkConnectorPoints, mkConnectorPath} from "./process-diagram-utils";

    export let connections = [];
    export let layoutDataById = {};

</script>

<g class="connections">
    {#each connections as conn}
        <path d={ mkConnectorPath(layoutDataById, conn)}>
        </path>
    {/each}
</g>

<style>
    path {
        stroke: #aaa;
        stroke-width: 2;
        fill: none;
    }
</style>
