<script>

    import EntitySearchSelector from "../../../common/svelte/EntitySearchSelector.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import UsageTree from "./UsageTree.svelte";
    import {databaseInformationStore} from "../../../svelte-stores/database-information-store";
    import {serverInformationStore} from "../../../svelte-stores/server-information-store";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {serverUsageStore} from "../../../svelte-stores/server-usage-store";
    import {customEnvironmentUsageStore} from "../../../svelte-stores/custom-environment-usage-store";
    import {mkRef} from "../../../common/entity-utils";
    import MiniActions from "../../../common/svelte/MiniActions.svelte";
    import {combineServerData, doAssetSearch} from "./custom-environment-utils";
    import {termSearch} from "../../../common";

    export let usages;
    export let doCancel;
    export let application;
    export let environment;

    const databaseSearchFields = [
        'databaseName',
        'environment',
        'instanceName',
        'dbmsVendor',
        'dbmsName'
    ];

    const serverSearchFields = [
        'serverInformation.hostname',
        'serverInformation.operatingSystem',
        'serverInformation.operatingSystemVersion',
        'serverInformation.location',
        'serverInformation.country',
        'serverUsage.environment'
    ];


    let invalid = true;
    let savePromise;
    let removePromise;

    let showSearch = true;
    let qry;

    let databasesInGroup;
    let serverUsagesInGroup;

    let selectedTab = 'servers';

    let databaseActions = [
        {
            icon: 'trash',
            handleAction: (database) => removeAsset(database),
            description: "Click to remove database from custom environment"
        }
    ];
    let serverActions = [
        {
            icon: 'trash',
            handleAction: (server) => removeAsset(server),
            description: "Click to remove server from custom environment"
        },
    ];
    let applicationActions = [
        {
            icon: 'pencil-square-o',
            handleAction: (application) => {
                showSearch = true;
                searchApplication = application;
            },
            description: "Click to search databases and severs associated to this application"
        }
    ];
    let assetActions = [
        {
            name: "add",
            icon: 'plus',
            handleAction: (d) => addAsset(d.assetRef),
            description: "Click to remove database from custom environment"
        }
    ];

    $: searchApplication = application; // default to the owing app
    $: [databasesInGroup, serverUsagesInGroup] = _.partition(usages, d => d.usage.entityReference.kind === 'DATABASE');

    $: databaseIdsInGroup = _.map(databasesInGroup, d => d.usage.entityReference.id);
    $: serverUsageIdsInGroup = _.map(serverUsagesInGroup, d => d.usage.entityReference.id);

    function addAsset(ref) {
        const usage = {
            customEnvironmentId: environment.id,
            entityReference: ref,
            createdBy: 'pending'
        }
        return savePromise = customEnvironmentUsageStore.addAsset(usage)
            .then(() => customEnvironmentUsageStore.findUsageInfoByOwningEntityRef(application, true));
    }

    $: databaseInformationCall = searchApplication && databaseInformationStore.findByAppId(searchApplication.id);
    $: serverInformationCall = searchApplication && serverInformationStore.findByAppId(searchApplication.id);
    $: serverUsageCall = searchApplication && serverUsageStore.findByEntityRef(searchApplication);

    $: databaseAssets = $databaseInformationCall?.data;
    $: serverAssets = combineServerData($serverInformationCall?.data, $serverUsageCall?.data);

    $: databaseSearchResults = termSearch(
        databaseAssets,
        qry,
        databaseSearchFields,
        d => _.includes(databaseIdsInGroup, d.id));

    $: serverSearchResults = termSearch(
        serverAssets,
        qry,
        serverSearchFields,
        d => _.includes(serverUsageIdsInGroup, d.serverUsage.id));

    function cancel() {
        return doCancel();
    }


    function onSelectApplication(e) {
        searchApplication = e.detail;
    }


    function removeAsset(assetUsage) {
        removePromise = customEnvironmentUsageStore.remove(assetUsage.id)
            .then(() => customEnvironmentUsageStore.findUsageInfoByOwningEntityRef(application, true));
    }

    function toDatabaseContext(result){
        const ref = mkRef('DATABASE', result.id);
        return {assetRef: ref, environmentId: environment.id}

    }

    function toServerUsageContext(result){
        const ref = mkRef('SERVER_USAGE', result.serverUsage.id);
        return {assetRef: ref, environmentId: environment.id}

    }

    function clearSearchApp(){
        return searchApplication = null;
    }

</script>

{#if !_.isEmpty(usages)}
    <UsageTree {usages} {databaseActions} {serverActions} {applicationActions}>
    </UsageTree>
    {:else}
    <div>No databases or servers have been associated to this environment.</div>
{/if}


{#if !showSearch}
    <button class="btn btn-link" on:click={() => showSearch = true}>
        Search more assets
    </button>
{/if}

{#if showSearch}
    <br>
    {#if !searchApplication}
        <div class="form-group">
            <label for="searchApplication">Change asset owing application:</label>
            <div id="searchApplication">
                    <EntitySearchSelector entityKinds={['APPLICATION']}
                                          on:select={onSelectApplication}/>
                    <p class="text-muted">Select an application to show associated servers and databases</p>
            </div>
        </div>
        <MiniActions actions={[
            {name: "Cancel", icon: "times", handleAction:cancel}
        ]}/>
    {/if}

    {#if searchApplication}
        <div class="waltz-tabs">
            <!-- TAB HEADERS -->
            <input type="radio"
                   bind:group={selectedTab}
                   value="servers"
                   id="servers">
            <label class="wt-label"
                   for="servers">
                <span>Servers</span>
                <span class="text-muted">({serverSearchResults.length})</span>
            </label>

            <input type="radio"
                   bind:group={selectedTab}
                   value="databases"
                   id="databases">
            <label class="wt-label"
                   for="databases">
                <span>Databases</span>
                <span class="text-muted">({databaseSearchResults.length})</span>
            </label>

            <div class="wt-tab wt-active">
                <!-- SERVERS -->
                {#if selectedTab === 'servers'}
                    <p class="text-muted">
                        Showing servers associated to application: <b>{searchApplication.name}</b>.
                        <br>
                        Use the search control below to filter on name, environment etc (e.g. 'PROD Linux').
                    </p>
                    <SearchInput bind:value={qry}/>
                    <br>

                    <div class="waltz-scroll-region-300">
                        <table class="table table-condensed">
                            <thead>
                            <th>Environment</th>
                            <th>Hostname</th>
                            <th>Location</th>
                            <th>Country</th>
                            <th>OS</th>
                            <th>OS Version</th>
                            <th></th>
                            </thead>
                            <tbody>
                            {#each serverSearchResults as result}
                                <tr class="asset-row">
                                    <td>{result.serverUsage.environment}</td>
                                    <td>{result.serverInformation.hostname}</td>
                                    <td>{result.serverInformation.location}</td>
                                    <td>{result.serverInformation.country}</td>
                                    <td>{result.serverInformation.operatingSystem}</td>
                                    <td>{result.serverInformation.operatingSystemVersion}</td>
                                    <td>
                                        <MiniActions ctx={toServerUsageContext(result)}
                                                     actions={assetActions}/>
                                    </td>
                                </tr>
                            {:else}
                                {#if $serverInformationCall.status === 'loading'}
                                    <tr class="loading">
                                        <td colspan="7">
                                            Loading servers
                                        </td>
                                    </tr>
                                {/if}
                                {#if $serverInformationCall.status === 'loaded'}
                                    <tr class="no-data">
                                        <td colspan="7">
                                            No servers found
                                        </td>
                                    </tr>
                                {/if}
                            {/each}
                            </tbody>
                        </table>
                    </div>

                <!-- DATABASES -->
                {:else if selectedTab === 'databases'}
                    <p class="text-muted">
                        Showing databases associated to application: <b>{searchApplication.name}</b>.
                        <br>
                        Use the search control below to filter on name, environment etc (e.g. 'PROD Oracle').
                    </p>
                    <SearchInput bind:value={qry}/>
                    <br>

                    <div class="waltz-scroll-region-300">
                        <table class="table table-condensed">
                            <thead>
                            <th>Environment</th>
                            <th>Name</th>
                            <th>Instance</th>
                            <th>DBMS Name</th>
                            <th>DBMS Vendor</th>
                            <th></th>
                            </thead>
                            <tbody>
                            {#each databaseSearchResults as result}
                                <tr class="asset-row">
                                    <td>{result.environment}</td>
                                    <td>{result.databaseName}</td>
                                    <td>{result.instanceName}</td>
                                    <td>{result.dbmsName}</td>
                                    <td>{result.dbmsVendor}</td>
                                    <td>
                                        <MiniActions ctx={toDatabaseContext(result)}
                                                     actions={assetActions}/>
                                    </td>
                                </tr>
                            {:else}
                                {#if $databaseInformationCall.status === 'loading'}
                                    <tr class="loading">
                                        <td colspan="7">
                                            Loading databases
                                        </td>
                                    </tr>
                                {/if}
                                {#if $databaseInformationCall.status === 'loaded'}
                                    <tr class="no-data">
                                        <td colspan="7">
                                            No databases found
                                        </td>
                                    </tr>
                                {/if}
                            {/each}
                            </tbody>
                        </table>
                    </div>
                {/if} <!-- end tabs if -->
            </div>
        </div>
        <br>
        <MiniActions actions={[
            {name: "Cancel", icon: "times", handleAction:cancel},
            {name: `Change Focus Application (currently ${searchApplication.name})`, icon: "random", handleAction: clearSearchApp}
        ]}/>
    {/if}
{/if}


{#if savePromise}
    {#await savePromise}
        Saving...
    {:then r}
        Saved!
    {:catch e}
        <div class="alert alert-warning">
            Failed to save asset to {environment.name}. Reason: {e?.data?.message}
            <button class="btn-link"
                    on:click={() => savePromise = null}>
                <Icon name="check"/>
                Okay
            </button>
        </div>
    {/await}
{/if}
{#if removePromise}
    {#await removePromise}
        Removing...
    {:then r}
        Removed!
    {:catch e}
        <div class="alert alert-warning">
            Failed to remove asset from {environment.name}. Reason: {e.data.message}
            <button class="btn-link"
                    on:click={() => removePromise = null}>
                <Icon name="check"/>
                Okay
            </button>
        </div>
    {/await}
{/if}

<style>
    .asset-row td {
        padding-bottom: 0;
        /*margin: 0;*/
        /*border: none;*/
    }
    .loading {
        background-color: #f6f5f0;
    }
    .no-data {
        background-color: #f6f0f0;
    }
</style>