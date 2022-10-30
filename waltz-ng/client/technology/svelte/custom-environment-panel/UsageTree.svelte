<script>
    import Icon from "../../../common/svelte/Icon.svelte";
    import {groupUsagesByApplication} from "./custom-environment-utils";
    import MiniActions from "../../../common/svelte/MiniActions.svelte";
    import {createEventDispatcher} from "svelte";

    const dispatch = createEventDispatcher();

    export let usages;
    export let databaseActions = [];
    export let serverActions = [];

    export let applicationActions = [];

    $: usagesGroupedByApplication = groupUsagesByApplication(usages);

    function selectAsset(asset) {
        dispatch("select", asset);
    }

</script>

<ul>
    {#each usagesGroupedByApplication as usageInfo}
        <li>
            <Icon name="desktop"/>
            <span>{usageInfo.application.name}</span>
            <MiniActions ctx={usageInfo.application} actions={applicationActions}/>
            <ul style="padding-left: 3em">
                {#if usageInfo.serverUsages.length > 0}
                    <li>
                        <Icon name="server"/>
                        Servers
                    </li>
                    <ul style="padding-left: 3em">
                        {#each usageInfo.serverUsages as server}
                            <li class="waltz-visibility-parent"
                                on:click,keydown={() => selectAsset(server)}
                                data-env-usage-id={server.usage.id}
                                data-server-usage-id={server.usage.entityReference.id}
                                data-server-id={server.asset.id}>
                                <span class="waltz-visibility-child-30">
                                    <Icon name="circle"/>
                                </span>
                                <span>{server.asset.hostname}</span>
                                <span class="waltz-visibility-child-30">
                                    <MiniActions ctx={server.usage} actions={serverActions}/>
                                </span>
                            </li>
                        {/each}
                    </ul>
                {/if}
                {#if usageInfo.databaseUsages.length > 0}
                    <li>
                        <Icon name="database"/>
                        Databases
                    </li>
                    <ul style="padding-left: 3em">
                        {#each usageInfo.databaseUsages as database}
                            <li class="waltz-visibility-parent"
                                on:click,keydown={() => selectAsset(database)}
                                data-env-usage-id={database.usage.id}
                                data-database-id={database.asset.id}>
                                <span class="waltz-visibility-child-30">
                                    <Icon name="circle"/>
                                </span>
                                <span>
                                    {database.asset.databaseName}
                                </span>
                                <span class="waltz-visibility-child-30">
                                    <MiniActions ctx={database.usage} actions={databaseActions}/>
                                </span>
                            </li>
                        {/each}
                    </ul>
                {/if}
            </ul>
        </li>
    {/each}
</ul>

<style>
    ul {
        padding: 0.2em 0 0 0;
        margin: 0 0 0 0;
        list-style: none;
    }

    li {
        padding-top: 0.2em;
    }

</style>

