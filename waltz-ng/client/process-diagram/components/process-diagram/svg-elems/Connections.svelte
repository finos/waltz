<script>
    import {connections, highlightedConnections, layoutDataById} from "../diagram-store";
    import {mkConnectorPath, moveToFront} from "../process-diagram-utils";
    import {selectAll} from "d3-selection";

    $: {
        selectAll(".process-connection")
            .classed("highlight", false);

        _.forEach(
            $highlightedConnections,
            c => selectAll(`.process-connection-${c.connectorId}`)
                .classed("highlight", true)
                .each(function() {
                    moveToFront(this);
                }));
    }
</script>

<g class="connections">
    {#each $connections as conn}
        <path d={ mkConnectorPath($layoutDataById, conn)}
              class="process-connection process-connection-{conn.connectorId}">
        </path>
    {/each}
</g>

<style>
    path {
        stroke: #aaa;
        stroke-width: 2;
        fill: none;
        transition: stroke ease 0.4s;  /* does not fade in due to re-ordering (moveToFront) call */
    }

    .highlight {
        stroke: red;
        stroke-width: 3;
    }
</style>
