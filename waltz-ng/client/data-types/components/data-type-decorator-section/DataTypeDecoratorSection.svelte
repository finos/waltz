<script>

    import Icon from "../../../common/svelte/Icon.svelte";
    import DataTypeOverviewPanel from "./DataTypeOverviewPanel.svelte";
    import DataTypeDetailPanel from "./DataTypeDetailPanel.svelte";

    const Modes = {
        EDIT: "EDIT",
        VIEW: "VIEW"
    };

    const Tabs = {
        DATA_TYPES: "DATA_TYPES",
        DETAIL: "DETAIL"
    }

    let selectedTab = Tabs.DATA_TYPES;

    export let primaryEntityRef;

</script>


<div class="waltz-tabs" style="padding-top: 1em">
    <!-- TAB HEADERS -->
    <input type="radio"
           bind:group={selectedTab}
           value="{Tabs.DATA_TYPES}"
           id="{Tabs.DATA_TYPES}">
    <label class="wt-label"
           for="{Tabs.DATA_TYPES}">
        <span>Data Types</span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           value="{Tabs.DETAIL}"
           id="{Tabs.DETAIL}">
    <label class="wt-label"
           for="{Tabs.DETAIL}">
        <span>Detail</span>
    </label>

    <div class="wt-tab wt-active">
        {#if selectedTab === Tabs.DATA_TYPES}
            <DataTypeOverviewPanel primaryEntityReference={primaryEntityRef}/>
        {:else if selectedTab === Tabs.DETAIL}
            <DataTypeDetailPanel primaryEntityReference={primaryEntityRef}/>
        {/if}
    </div>
</div>
