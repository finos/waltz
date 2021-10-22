<script>
    import {highlightedConnections, selectedObject} from "../diagram-store";
    import _ from "lodash";
    import {selectDiagramObject} from "../process-diagram-utils";


    let inbound = [];
    let outbound = [];

    $: [outbound, inbound] = _.chain($highlightedConnections)
        .reject(c => c.hidden)
        .partition(c => c.startObjectId === $selectedObject?.objectId)
        .value();
</script>

{#if !_.isEmpty(inbound)}
    <table class="small table table-condensed table-hover">
        <colgroup>
            <col width="50%">
            <col width="50%">
        </colgroup>

        <thead>
        <th>Source</th>
        <th>Criteria</th>
        </thead>
        <tbody>
        {#each inbound as conn}
            <tr class="clickable"
                on:click={() => selectDiagramObject(conn.startObject)}>
                <td>{conn.startObject.name}</td>
                <td>{conn.name || '-'}</td>
            </tr>
        {/each}
        </tbody>
    </table>
{/if}

{#if !_.isEmpty(outbound)}
    <table class="small table table-condensed table-hover">
        <colgroup>
            <col width="50%">
            <col width="50%">
        </colgroup>
        <thead>
        <th>Target</th>
        <th>Criteria</th>
        </thead>
        <tbody>
        {#each outbound as conn}
            <tr class="clickable"
                on:click={() => selectDiagramObject(conn.endObject)}>
                <td>{conn.endObject.name}</td>
                <td>{conn.name || '-'}</td>
            </tr>
        {/each}
        </tbody>
    </table>
{/if}