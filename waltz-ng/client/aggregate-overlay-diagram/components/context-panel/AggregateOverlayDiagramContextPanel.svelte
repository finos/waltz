<script>
    import {getContext} from "svelte";
    import DiagramInstanceSelector from "../instance-selector/DiagramInstanceSelector.svelte";
    import CustomiseOverlayPanel from "./CustomiseOverlayPanel.svelte";

    export let primaryEntityRef;

    const svgDetail = getContext("svgDetail");

    let selectedInstance = getContext("selectedInstance");
    let selectedDiagram = getContext("selectedDiagram");
    let focusWidget = getContext("focusWidget");
    let selectedTab = 'widgets';

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
        <span>Configuration</span>
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
        {#if selectedTab === 'widgets'}

            <CustomiseOverlayPanel {primaryEntityRef}/>

        {:else if selectedTab === 'instances'}

            <DiagramInstanceSelector {primaryEntityRef}
                                     on:select={selectInstance}/>

        {/if}
    </div>
</div>