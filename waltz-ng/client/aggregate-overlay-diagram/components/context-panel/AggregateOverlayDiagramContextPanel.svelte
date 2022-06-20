<script>
    import {getContext} from "svelte";
    import DescriptionFade from "../../../common/svelte/DescriptionFade.svelte";
    import DiagramInstanceSelector from "../instance-selector/DiagramInstanceSelector.svelte";
    import CustomiseOverlayPanel from "./CustomiseOverlayPanel.svelte";
    import ImageDownloadLink from "../../../common/svelte/ImageDownloadLink.svelte";


    export let primaryEntityRef;

    const svgDetail = getContext("svgDetail");

    let selectedInstance = getContext("selectedInstance");
    let selectedDiagram = getContext("selectedDiagram");
    let focusWidget = getContext("focusWidget");

    let selectedTab = 'widgets';
    let generatingDiagram = false;


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
            <CustomiseOverlayPanel {primaryEntityRef}/>

        {:else if selectedTab === 'info'}

            <h4>{$selectedDiagram.name}</h4>
            <DescriptionFade text={$selectedDiagram.description}/>


            {#if $focusWidget?.legend}
                <hr>
                <svelte:component this={$focusWidget.legend}/>
            {/if}

            <hr>

            <ImageDownloadLink styling="link"
                               element={$svgDetail}
                               filename={`${$selectedDiagram.name}-image.png`}/>


        {:else if selectedTab === 'instances'}
            <DiagramInstanceSelector {primaryEntityRef}
                                     on:select={selectInstance}/>
        {/if}
    </div>
</div>