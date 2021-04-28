<script>
    import Icon from "../../../common/svelte/Icon.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";

    export let usages;

    let servers = [];
    let databases = [];

    $: [servers, databases] = _.partition(
        usages,
        d => d.usage.entityReference.kind === 'SERVER_USAGE');

</script>

<h5>
    <Icon name="server"/>
    Servers
</h5>
<table class="table table-condensed small table-hover">
    <thead>
    <tr>
        <th>Application Name</th>
        <th>Hostname</th>
        <th>OS</th>
        <th>OS Version</th>
        <th>Location</th>
        <th>Country</th>
        <th>HW EOL</th>
        <th>OS EOL</th>
        <th>Virtual?</th>
        <th>Usage Provenance</th>
    </tr>
    </thead>
    <tbody>
    {#each servers as usageInfo}
        <tr>
            <td><EntityLink ref={usageInfo.owningApplication}/></td>
            <td>{usageInfo.asset.hostname}</td>
            <td>{usageInfo.asset.operatingSystem}</td>
            <td>{usageInfo.asset.operatingSystemVersion}</td>
            <td>{usageInfo.asset.location}</td>
            <td>{usageInfo.asset.country}</td>
            <td>{usageInfo.asset.hardwareEndOfLifeDate || "-"}</td>
            <td>{usageInfo.asset.operatingSystemEndOfLifeDate || "-"}</td>
            <td>{@html usageInfo.asset.virtual ? "&check;" : ""}</td>
            <td>{usageInfo.usage.provenance}</td>
        </tr>
    {/each}
    </tbody>
</table>


<h5>
    <Icon name="database"/>
    Databases
</h5>
<table class="table table-condensed small table-hover">
    <thead>
    <tr>
        <th>Application Name</th>
        <th>Database</th>
        <th>Instance</th>
        <th>Vendor</th>
        <th>DBMS Name</th>
        <th>Version</th>
        <th>EOL Date</th>
        <th>Provenance</th>
    </tr>
    </thead>
    <tbody>
    {#each databases as usageInfo}
        <tr>
            <td><EntityLink ref={usageInfo.owningApplication}/></td>
            <td>{usageInfo.asset.databaseName}</td>
            <td>{usageInfo.asset.instanceName}</td>
            <td>{usageInfo.asset.dbmsVendor}</td>
            <td>{usageInfo.asset.dbmsName}</td>
            <td>{usageInfo.asset.dbmsVersion}</td>
            <td>{usageInfo.usage.endOfLifeDate || "-"}</td>
            <td>{usageInfo.usage.provenance}</td>
        </tr>
    {/each}
    </tbody>
</table>