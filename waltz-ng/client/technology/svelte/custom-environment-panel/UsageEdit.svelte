<script>

    import EntitySearchSelector from "../../../common/svelte/EntitySearchSelector.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import UsageTree from "./UsageTree.svelte";
    import {databaseInformationStore} from "../../../svelte-stores/database-information-store";
    import {serverInformationStore} from "../../../svelte-stores/server-information-store";
    import {termSearch} from "../../../common";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {serverUsageStore} from "../../../svelte-stores/server-usage-store";
    import {customEnvironmentUsageStore} from "../../../svelte-stores/custom-environment-usage-store";
    import {mkRef} from "../../../common/entity-utils";
    import EntityLabel from "../../../common/svelte/EntityLabel.svelte";
    import MiniActions from "../../../common/svelte/MiniActions.svelte";

    export let usages;
    export let doCancel;
    export let primaryEntityRef;
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
    let assetToAdd;
    let searchApplication = null;
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
            icon: 'plus',
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

    $: [databasesInGroup, serverUsagesInGroup] = _.partition(usages, d => d.usage.entityReference.kind === 'DATABASE');

    $: databaseIdsInGroup = _.map(databasesInGroup, d => d.usage.entityReference.id);
    $: serverUsageIdsInGroup = _.map(serverUsagesInGroup, d => d.usage.entityReference.id);

    function combineServerData(servers, usages) {
        if (_.isEmpty(servers) || _.isEmpty(usages)) {
            return [];
        } else {
            const serversById = _.keyBy(servers, d => d.id);
            return _
                .chain(usages)
                .map(u => Object.assign({}, {serverUsage: u, serverInformation: serversById[u.serverId]}))
                .filter(s => s.serverInformation != null)
                .value();
        }
    }

    function doDatabaseSearch(qry, databaseAssets, databaseIdsInGroup) {
        const searchResults = termSearch(databaseAssets, qry, databaseSearchFields);
        return _
            .chain(searchResults)
            .filter(d => !_.includes(databaseIdsInGroup, d.id))
            .take(15)
            .value();

    }

    function doServerSearch(qry, serverAssets, serverUsageIdsInGroup) {
        const searchResults = termSearch(serverAssets, qry, serverSearchFields);
        return _
            .chain(searchResults)
            .filter(d => !_.includes(serverUsageIdsInGroup, d.serverUsage.id))
            .take(15)
            .value();
    }

    function addAsset(ref) {
        const usage = {
            customEnvironmentId: environment.id,
            entityReference: ref,
            createdBy: 'admin'
        }
        return savePromise = customEnvironmentUsageStore.addAsset(usage)
            .then(() => customEnvironmentUsageStore.findUsageInfoByOwningEntityRef(primaryEntityRef, true));
    }

    $: databaseInformationCall = searchApplication && databaseInformationStore.findByAppId(searchApplication.id);
    $: serverInformationCall = searchApplication && serverInformationStore.findByAppId(searchApplication.id);
    $: serverUsageCall = searchApplication && serverUsageStore.findByEntityRef(searchApplication);

    $: databaseAssets = $databaseInformationCall?.data;
    $: serverAssets = combineServerData($serverInformationCall?.data, $serverUsageCall?.data);

    $: databaseSearchResults = doDatabaseSearch(qry, databaseAssets, databaseIdsInGroup);
    $: serverSearchResults = doServerSearch(qry, serverAssets, serverUsageIdsInGroup);


    function cancel() {
        return doCancel();
    }


    function onSelectApplication(e) {
        searchApplication = e.detail;
    }


    function removeAsset(assetUsage) {
        removePromise = customEnvironmentUsageStore.remove(assetUsage.id)
            .then(() => customEnvironmentUsageStore.findUsageInfoByOwningEntityRef(primaryEntityRef, true));
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
    <hr>
    {#if !searchApplication}
        <div class="form-group">
            <label for="searchApplication">Application:</label>
            <div id="searchApplication">
                    <EntitySearchSelector entityKinds={['APPLICATION']}
                                          on:select={onSelectApplication}/>
                    <p class="text-muted">Start typing to select an application to filter servers and databases</p>
            </div>
        </div>
    {/if}

    {#if searchApplication}
        <h3>
            <EntityLabel ref={searchApplication}/>
            <button class="btn btn-skinny"
                    title="Clear search application"
                    on:click={() => clearSearchApp()}>
                (<Icon name="times"/>)
            </button>
        </h3>

        <SearchInput bind:value={qry}/>
        <p class="text-muted">Start typing to filter servers and databases by name, environment etc.</p>

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
                {#if selectedTab === 'databases'}
                    <table class="table table-condensed small">
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
                            <tr>
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
                        {/each}
                        </tbody>
                    </table>
                {:else if selectedTab === 'servers'}
                    <table class="table table-condensed small">
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
                            <tr>
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
                        {/each}
                        </tbody>
                    </table>
                {/if} <!-- end tabs if -->
            </div>
        </div>
    {/if}
{/if}

<button class="btn btn-default"
        on:click={cancel}>
    Cancel
</button>

{#if savePromise}
    {#await savePromise}
        Saving...
    {:then r}
        Saved!
    {:catch e}
        <div class="alert alert-warning">
            Failed to save asset to {environment.name}. Reason: {e.data.message}
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

