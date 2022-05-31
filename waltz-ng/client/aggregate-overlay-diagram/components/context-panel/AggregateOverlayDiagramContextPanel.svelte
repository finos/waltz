<script>

    import {getContext} from "svelte";
    import WidgetSelector from "../aggregate-overlay-diagram/WidgetSelector.svelte";
    import DescriptionFade from "../../../common/svelte/DescriptionFade.svelte";
    import DiagramInstanceSelector from "../instance-selector/DiagramInstanceSelector.svelte";
    import SelectedOverlayPanel from "./SelectedOverlayPanel.svelte";
    import FilterSelectorPanel from "../filter-selector/FilterSelectorPanel.svelte";

    export let primaryEntityRef;

    let selectedInstance = getContext("selectedInstance");
    let selectedDiagram = getContext("selectedDiagram");

    let selectedTab = 'widgets'

    function selectInstance(evt) {
        $selectedInstance = evt.detail;
    }
</script>

<div class="row waltz-tabs"
     style="padding-top: 1em">
    <!-- TAB HEADERS -->
    <input type="radio"
           bind:group={selectedTab}
           value="widgets"
           id="widgets">
    <label class="wt-label"
           for="widgets">
        <span>Overlays</span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           value="info"
           id="info">
    <label class="wt-label"
           for="info">
        <span>Info</span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           value="instances"
           id="instances">
    <label class="wt-label"
           for="instances">
        <span>Instances</span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           value="filters"
           id="filters">
    <label class="wt-label"
           for="filters">
        <span>Filters</span>
    </label>

    <div class="wt-tab wt-active">
        <!-- SERVERS -->
        {#if selectedTab === 'widgets'}

            <WidgetSelector {primaryEntityRef}/>

            <hr>

            <SelectedOverlayPanel/>

        {:else if selectedTab === 'info'}

            <h4>{$selectedDiagram.name}</h4>
            <DescriptionFade text={$selectedDiagram.description}/>

        {:else if selectedTab === 'instances'}
            <DiagramInstanceSelector {primaryEntityRef}
                                     on:select={selectInstance}/>

        {:else if selectedTab === 'filters'}
            <FilterSelectorPanel/>
        {/if}
    </div>
</div>