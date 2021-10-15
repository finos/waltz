<script>
    import {highlightedConnections, selectedObject} from "../diagram-store";
    import _ from "lodash";
    import {selectDiagramObject} from "../process-diagram-utils";

    $: [outbound, inbound] = _.chain($highlightedConnections)
        .reject(c => c.hidden)
        .partition(c => c.startObjectId === $selectedObject?.objectId)
        .value();
</script>

<h4>&#8413; {$selectedObject.name}</h4>

<dl>
    <dt>Stereotype</dt>
    <dd>{$selectedObject.stereotype}</dd>
</dl>

{#if !_.isEmpty(inbound)}
    <table class="small table table-condensed table-hover">
        <colgroup>
            <col width="50%">
            <col width="50%">
        </colgroup>

        <thead>
        <th>Input</th>
        <th>Source</th>
        </thead>
        <tbody>
        {#each inbound as conn}
            <tr class="clickable"
                on:click={() => selectDiagramObject(conn.startObject)}>
                <td>{conn.name || '-'}</td>
                <td>{conn.startObject.name}</td>
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
        <th>Output</th>
        <th>Target</th>
        </thead>
        <tbody>
        {#each outbound as conn}
            <tr class="clickable"
                on:click={() => selectDiagramObject(conn.endObject)}>
                <td>{conn.name || '-'}</td>
                <td>{conn.endObject.name}</td>
            </tr>
        {/each}
        </tbody>
    </table>
{/if}