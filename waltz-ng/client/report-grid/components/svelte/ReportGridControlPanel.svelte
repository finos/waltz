<script>

    import ReportGridOverview from "./ReportGridOverview.svelte";
    import ReportGridFilters from "./ReportGridFilters.svelte";
    import ColumnDefinitionEditPanel from "./column-definition-edit-panel/ColumnDefinitionEditPanel.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import ReportGridPersonEditPanel from "./person-edit-panel/ReportGridPersonEditPanel.svelte";
    import {gridService} from "./report-grid-service";
    import {tabs} from "./report-grid-ui-service";

    export let onGridSelect = () => console.log("selecting grid");
    export let onSave = () => console.log("Saved report grid");
    export let primaryEntityRef;

    const {gridDefinition, userRole} = gridService;

    let selectedTab = tabs.OVERVIEW;

    function handleGridSelect(selectedGrid, isNew) {
        if (isNew) {
            selectedTab = tabs.COLUMNS;
        }
        onGridSelect(selectedGrid);
    }

    $: isOwned = $userRole === "OWNER";

</script>

<div class="waltz-tabs" style="padding-top: 1em">
    <!-- TAB HEADERS -->

    <input type="radio"
           bind:group={selectedTab}
           value={tabs.OVERVIEW}
           id="overview">
    <label class="wt-label"
           for="overview">
        <span>
            Overview
            {#if $gridDefinition}{` - ${$gridDefinition.name}`}{/if}
        </span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           value={tabs.FILTERS}
           disabled={!$gridDefinition}
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
        {#if selectedTab === 'overview'}
            <ReportGridOverview onGridSelect={handleGridSelect}
                                {primaryEntityRef}/>
        {:else if selectedTab === 'filters'}
            <ReportGridFilters {primaryEntityRef}/>
        {:else if selectedTab === 'columns'}
            <ColumnDefinitionEditPanel gridId={$gridDefinition?.id}
                                       columnDefs={$gridDefinition?.columnDefinitions}
                                       onSave={onSave}/>
        {:else if selectedTab === 'people'}
            <ReportGridPersonEditPanel/>
        {/if}
    </div>
</div>
