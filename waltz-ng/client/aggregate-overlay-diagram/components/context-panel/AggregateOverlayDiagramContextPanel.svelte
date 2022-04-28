<script>

    import CalloutList from "../aggregate-overlay-diagram/callout/CalloutList.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import {getContext} from "svelte";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";
    import WidgetSelector from "../aggregate-overlay-diagram/WidgetSelector.svelte";
    import DescriptionFade from "../../../common/svelte/DescriptionFade.svelte";
    import DiagramInstanceSelector from "../instance-selector/DiagramInstanceSelector.svelte";

    export let primaryEntityRef;

    let selectedInstance = getContext("selectedInstance");
    let selectedDiagram = getContext("selectedDiagram");

    let selectedTab = 'widgets'

    function selectInstance(evt) {
        $selectedInstance = evt.detail;
    }
</script>

<div class="waltz-tabs"
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

    <div class="wt-tab wt-active">
        <!-- SERVERS -->
        {#if selectedTab === 'widgets'}

            <WidgetSelector {primaryEntityRef}/>

        {:else if selectedTab === 'info'}

            <h4>{$selectedDiagram.name}</h4>
            <DescriptionFade text={$selectedDiagram.description}/>

        {:else if selectedTab === 'instances'}
            <DiagramInstanceSelector {primaryEntityRef}
                                     on:select={selectInstance}/>
        {/if}
    </div>
</div>