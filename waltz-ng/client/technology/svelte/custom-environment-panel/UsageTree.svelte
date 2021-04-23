<script>
    import Icon from "../../../common/svelte/Icon.svelte";
    import {groupUsagesByApplication} from "./custom-environment-utils";
    import MiniActions from "../../../common/svelte/MiniActions.svelte";

    export let usages;
    export let databaseActions = [];
    export let serverActions = [];
    export let applicationActions = [];

    $: usagesGroupedByApplication = groupUsagesByApplication(usages);


</script>
<ul>
    {#each usagesGroupedByApplication as usageInfo}
        <li>
            <Icon name="desktop"/>
            <span>{usageInfo.application.name}</span>
            <MiniActions ctx={usageInfo.application} actions={applicationActions}/>
            <ul style="padding-left: 2em">
                {#if usageInfo.serverUsages.length > 0}
                    <li>
                        Servers
                    </li>
                    <ul style="padding-left: 2em">
                        {#each usageInfo.serverUsages as server}
                            <li>
                                <Icon name="server"/>
                                <span>{server.asset.hostname}</span>
                                <MiniActions ctx={server.usage} actions={serverActions}/>
                            </li>
                        {/each}
                    </ul>
                {/if}
                {#if usageInfo.databaseUsages.length > 0}
                    <li>
                        Databases
                    </li>
                    <ul style="padding-left: 2em">
                        {#each usageInfo.databaseUsages as database}
                            <li>
                                <Icon name="database"/>
                                <span>
                                    {database.asset.databaseName}
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

