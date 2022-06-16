<script>
    import {getContext} from "svelte";
    import DescriptionFade from "../../../common/svelte/DescriptionFade.svelte";
    import DiagramInstanceSelector from "../instance-selector/DiagramInstanceSelector.svelte";
    import CustomiseOverlayPanel from "./CustomiseOverlayPanel.svelte";
    import html2canvas from "html2canvas";
    import Icon from "../../../common/svelte/Icon.svelte";


    export let primaryEntityRef;

    let selectedInstance = getContext("selectedInstance");
    let selectedDiagram = getContext("selectedDiagram");
    let focusWidget = getContext("focusWidget");

    let selectedTab = 'widgets';
    let generatingDiagram = false;

    function selectInstance(evt) {
        $selectedInstance = evt.detail;
    }

    function exportDiagram() {

        const element = document.querySelector("#diagram-capture");

        if (element) {

            generatingDiagram = true;
            //Using a timeout so browser has chance to display the progress icon
            setTimeout(() => {
                    return html2canvas(element)
                        .then(canvas => {
                            document.body.appendChild(canvas);
                            return canvas;
                        })
                        .then(canvas => {
                            const image = canvas.toDataURL('image/png').replace('image/png', 'image/octet-stream');
                            const a = document.createElement('a');
                            a.setAttribute('download', `${$selectedDiagram.name}-image.png`);
                            a.setAttribute('href', image);
                            a.click();
                            canvas.remove();
                        })
                        .then(() => generatingDiagram = false);
                },
                0);

        }
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

            <button class="btn btn-link"
                    on:click={() => exportDiagram()}
                    disabled={generatingDiagram}>
                {#if generatingDiagram}
                    <Icon fixedWidth="true"
                          name="refresh"
                          spin="true"/>
                {:else}
                    <Icon fixedWidth="true"
                          name="cloud-download"/>
                {/if}
                Export diagram
            </button>


        {:else if selectedTab === 'instances'}
            <DiagramInstanceSelector {primaryEntityRef}
                                     on:select={selectInstance}/>
        {/if}
    </div>
</div>