<script>

    import {highlightedConnections, selectedObject} from "./diagram-store";

    let inbound = [];
    let outbound = [];

    $:  [outbound, inbound] = _.partition($highlightedConnections, c => c.startObjectId === $selectedObject?.objectId);

</script>

<h1>Decision selected</h1>

<h4>Inputs</h4>
<table class="table table-condensed table-hover table-striped">
    <thead>
    <th>Decision Criteria</th>
    <th>Source</th>
    </thead>
    <tbody>
    {#each inbound as conn}
        <tr class="clickable"
            on:click={() => $selectedObject = conn.startObject}>
            <td>{conn.name || '-'}</td>
            <td>{conn.startObject.name}</td>
        </tr>
    {/each}
    </tbody>
</table>

<h4>Outputs</h4>
<table class="table table-condensed table-hover table-striped">
    <thead>
    <th>Decision Criteria</th>
    <th>Target</th>
    </thead>
    <tbody>
    {#each outbound as conn}
        <tr class="clickable"
            on:click={() => $selectedObject = conn.endObject}>
            <td>{conn.name || '-'}</td>
            <td>{conn.endObject.name}</td>
        </tr>
    {/each}
    </tbody>
</table>
