<script>
    import Icon from "../../../common/svelte/Icon.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";

    export let usages;

</script>


<table class="table table-condensed table-hover">
    <thead>
    <th>Application Name</th>
    <th>Entity</th>
    <th>Created At</th>
    <th>Created By</th>
    <th>Provenance</th>
    </thead>
    <tbody>
    {#each usages as usageInfo}
        <tr>
            <td>
                <EntityLink ref={usageInfo.owningApplication}/></td>
            <td>
                <Icon name={usageInfo.asset.hostname ? "server" : "database"}/>
                {usageInfo.asset.hostname || usageInfo.asset.databaseName}</td>
            <td>{usageInfo.usage.createdAt}</td>
            <td>{usageInfo.usage.createdBy}</td>
            <td>{usageInfo.usage.provenance}</td>
        </tr>
    {/each}
    </tbody>
</table>