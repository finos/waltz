<script>

    import ReportGridOverview from "./ReportGridOverview.svelte";
    import ReportGridFilters from "./ReportGridFilters.svelte";
    import ColumnDefinitionEditPanel from "./column-definition-edit-panel/ColumnDefinitionEditPanel.svelte";
    import {selectedGrid} from "./report-grid-store";

    export let onGridSelect = () => console.log("selecting grid");
    export let onSave = () => console.log("Saved report grid");
    let selectedTab = "context"

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
           disabled={!$selectedGrid}
           id="filters">
    <label class="wt-label"
           for="filters">
        <span>Filters</span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           disabled={!$selectedGrid}
           value="columns"
           id="columns">
    <label class="wt-label"
           for="columns">
        <span>Columns</span>
    </label>

    <div class="wt-tab wt-active">
        <!-- SERVERS -->
        {#if selectedTab === 'context'}
            <ReportGridOverview {onGridSelect}/>
        {:else if selectedTab === 'filters'}
            <ReportGridFilters/>
        {:else if selectedTab === 'columns'}
            <ColumnDefinitionEditPanel gridId={$selectedGrid?.definition.id}
                                       columnDefs={$selectedGrid?.definition.columnDefinitions}
                                       onSave={onSave}/>
        {/if}
    </div>
</div>
