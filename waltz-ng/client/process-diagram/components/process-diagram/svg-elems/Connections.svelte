<script>
    import {connections, layoutDataById} from "../diagram-store";
    import {mkConnectorPath} from "../process-diagram-utils";

</script>

<g class="connections">
    {#each $connections as conn}
        <path d={ mkConnectorPath($layoutDataById, conn)}>
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
