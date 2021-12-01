<script>

    import ReportGridOverview from "./ReportGridOverview.svelte";
    import ReportGridFilters from "./ReportGridFilters.svelte";

    export let selectedGrid;
    export let onGridSelect = () => console.log("selecting grid");
    let selectedTab = "context"

    $: console.log({selectedGrid});

</script>

<div class="waltz-tabs" style="padding-top: 1em">
    <!-- TAB HEADERS -->
    <input type="radio"
           bind:group={selectedTab}
           value="context"
           id="context">
    <label class="wt-label"
           for="context">
        <span>Context</span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           value="filters"
           id="filters">
    <label class="wt-label"
           for="filters">
        <span>Filters</span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           value="columns"
           id="columns">
    <label class="wt-label"
           for="columns">
        <span>Columns</span>
    </label>

    <div class="wt-tab wt-active">
        <!-- SERVERS -->
        {#if selectedTab === 'context'}
            <ReportGridOverview {selectedGrid} {onGridSelect}/>
        {:else if selectedTab === 'filters'}
            <ReportGridFilters/>
        {:else if selectedTab === 'columns'}
            <h4>Columns</h4>
        {/if}
    </div>
</div>
