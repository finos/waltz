<script>

    import ReportGridOverview from "./ReportGridOverview.svelte";
    import ReportGridFilters from "./ReportGridFilters.svelte";
    import ColumnDefinitionEditPanel from "./column-definition-edit-panel/ColumnDefinitionEditPanel.svelte";
    import {selectedGrid, ownedReportIds} from "./report-grid-store";
    import {reportGridStore} from "../../../svelte-stores/report-grid-store";
    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";
    import ReportGridPersonEditPanel from "./person-edit-panel/ReportGridPersonEditPanel.svelte";

    export let onGridSelect = () => console.log("selecting grid");
    export let onSave = () => console.log("Saved report grid");
    export let primaryEntityRef;
    export let showGridSelector = true;

    const tabs = {
        OVERVIEW: 'overview',
        FILTERS: 'filters',
        COLUMNS: 'columns',
        PEOPLE: 'people'
    };

    function determineStartingTab(showGridSelector) {
        return showGridSelector
            ? 'overview'
            : 'filters';
    }

    let selectedTab = determineStartingTab(showGridSelector)

    function handleGridSelect(selectedGrid, isNew) {
        if (isNew) {
            selectedTab = tabs.COLUMNS;
        }
        onGridSelect(selectedGrid);
    }

    $: isOwned = $selectedGrid && _.includes($ownedReportIds, $selectedGrid?.definition?.id);

    $: ownedGridsCall = $selectedGrid?.definition?.id && reportGridStore.findForOwner(true);
    $: $ownedReportIds = _.map($ownedGridsCall?.data, d => d.id);

</script>

<div class="waltz-tabs" style="padding-top: 1em">
    <!-- TAB HEADERS -->

    {#if showGridSelector}
        <input type="radio"
               bind:group={selectedTab}
               value={tabs.OVERVIEW}
               id="overview">
        <label class="wt-label"
               for="overview">
            <span>
                Overview
                {#if $selectedGrid}{` - ${$selectedGrid.definition.name}`}{/if}
            </span>
        </label>
    {/if}

    <input type="radio"
           bind:group={selectedTab}
           value={tabs.FILTERS}
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
           value={tabs.COLUMNS}
           id="columns">
    <label class="wt-label"
           for="columns">
        <span title={isOwned ? "" : "You are not an owner for this report grid"}>
            Column Editor
            <Icon name={isOwned ? "unlock" : "lock"}/>
        </span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           disabled={!isOwned}
           value={tabs.PEOPLE}
           id="people">
    <label class="wt-label"
           for="people">
        <span title={isOwned ? "" : "You are not an owner for this report grid"}>
            People Editor
            <Icon name={isOwned ? "unlock" : "lock"}/>
        </span>
    </label>

    <div class="wt-tab wt-active">
        <!-- SERVERS -->
        {#if selectedTab === 'overview'}
            <ReportGridOverview onGridSelect={handleGridSelect}/>
        {:else if selectedTab === 'filters'}
            <ReportGridFilters {primaryEntityRef}/>
        {:else if selectedTab === 'columns'}
            <ColumnDefinitionEditPanel gridId={$selectedGrid?.definition?.id}
                                       columnDefs={$selectedGrid?.definition?.columnDefinitions}
                                       onSave={onSave}/>
        {:else if selectedTab === 'people'}
            <ReportGridPersonEditPanel/>
        {/if}
    </div>
</div>
