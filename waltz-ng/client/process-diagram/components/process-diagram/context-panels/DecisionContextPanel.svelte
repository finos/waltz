<script>
    import {selectDiagramObject} from "../process-diagram-utils";
    import {highlightedConnections, selectedObject} from "../diagram-store";

    let inbound = [];
    let outbound = [];

    $:  [outbound, inbound] = _.partition($highlightedConnections, c => c.startObjectId === $selectedObject?.objectId);

</script>

<h4>&#8415; {$selectedObject.name || 'Decision'}</h4>

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
