<script>

    import ReportGridOverview from "./ReportGridOverview.svelte";
    import ReportGridFilters from "./ReportGridFilters.svelte";
    import ColumnDefinitionEditPanel from "./column-definition-edit-panel/ColumnDefinitionEditPanel.svelte";
    import {selectedGrid, ownedReportIds} from "./report-grid-store";
    import {reportGridStore} from "../../../svelte-stores/report-grid-store";
    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";

    export let onGridSelect = () => console.log("selecting grid");
    export let onSave = () => console.log("Saved report grid");
    let selectedTab = "overview"


    $: isOwned = $selectedGrid && _.includes($ownedReportIds, $selectedGrid.definition?.id);

    $: ownedGridsCall = $selectedGrid.definition.id && reportGridStore.findForOwner(true);
    $: $ownedReportIds = _.map($ownedGridsCall?.data, d => d.id);


</script>

<div class="waltz-tabs" style="padding-top: 1em">
    <!-- TAB HEADERS -->
    <input type="radio"
           bind:group={selectedTab}
           value="overview"
           id="overview">
    <label class="wt-label"
           for="overview">
        <span>
            Overview
        </span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           value="filters"
           disabled={!$selectedGrid}
           id="filters">
    <label class="wt-label"
           for="filters">
        <span>
            Filters
        </span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           disabled={!isOwned}
           value="columns"
           id="columns">
    <label class="wt-label"
           for="columns">
        <span title={isOwned ? "" : "You are not an owner for this report grid"}>
            Column Editor
            <Icon name={isOwned ? "unlock" : "lock"}/>
        </span>
    </label>

    <div class="wt-tab wt-active">
        <!-- SERVERS -->
        {#if selectedTab === 'overview'}
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
