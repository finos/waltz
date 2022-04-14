<script>
    import _ from "lodash";
    import WidgetSelector from "./WidgetSelector.svelte";
    import Callout from "./Callout.svelte";

    export let svg = "";
    export let primaryEntityRef;
    export let widgetComponent;
    export let dataProvider;

    let svgHolderElem;
    let renderedWidgetRefs = {}; // this gets populated by the calls to `bind:this`
    let renderedCalloutRefs = {}; // this gets populated by the calls to `bind:this`
    let documentationIconElem;

    $: console.log({primaryEntityRef});

    $: cellDataByCellExtId = _.keyBy($dataProvider?.data, d => d.cellExternalId);

    const callouts = [
        {cellId: "SURV", text: "Surveillance is up"},
        {cellId: "TRADE_MGMT", text: "Trade Management will change"},
        {cellId: "TREASURE", text: "Nothing happens"},
        {cellId: "AFC", text: "2025 is far away, maybe things will be different"},
    ]


    function renderOverlays(refs = [], targetSelector, setContentSize) {

        console.log({refs});

        _.each(refs, (v, k) => {
            if (!v) return;
            const cell = svgHolderElem.querySelector(`[data-cell-id=${k}]`);

            if (cell == null) {
                console.log("Cannot find cell for key:" + k);
                return;
            }

            const targetBox = cell.querySelector(targetSelector);

            if (!targetBox) {
                console.log("Cannot find target box for cell-id", k);
                return;
            }

            const contentRef = v.querySelector(".content");

            if (!contentRef) {
                console.log("Cannot find content section for copying into the target box for cell-id", k);
                return;
            }

            const boundingBox = targetBox.getBBox();

            setContentSize(boundingBox, contentRef);

            const existingContent = targetBox.querySelector(".content");
            if (existingContent) {
                targetBox.replaceChild(contentRef.cloneNode(true), existingContent);
            } else {
                targetBox.append(contentRef.cloneNode(true));
            }
        });
    }

    $: {
        if (svgHolderElem && renderedWidgetRefs) {
            renderOverlays(
                renderedWidgetRefs,
                ".statistics-box",
                (bBox, contentRef) => {
                    contentRef.setAttribute("width", bBox.width);
                    contentRef.setAttribute("height", bBox.height);
                });
            renderOverlays(
                renderedCalloutRefs,
                ".outer",
                (bBox, contentRef) => {

                    const size = bBox.height * 0.25;

                    contentRef.setAttribute("width", size);
                    contentRef.setAttribute("height", size);
                });
        }
    }

</script>

<div bind:this={svgHolderElem}>
    {@html svg}
</div>

{#key widgetComponent}  <!-- we want to destroy this section if the widget changes so the renderedWidgetRefs gets reset -->
    <div class="rendered-widgets">
        {#each _.keys(cellDataByCellExtId) as cellExtId, idx}
            <div bind:this={renderedWidgetRefs[cellExtId]}>
                <h4>Widget for cell: {cellExtId}</h4>
                <svelte:component this={widgetComponent}
                                  cellExtId={cellExtId}
                                  cellData={cellDataByCellExtId[cellExtId]}/>
            </div>
        {/each}
    </div>
{/key}

<div class="rendered-callouts">
    {#each callouts as callout, idx}
        <div bind:this={renderedCalloutRefs[callout.cellId]}>
            <h4>Callout for cell: {callout.cellId}</h4>
            <Callout {callout} label={idx}/>
        </div>
    {/each}
</div>


<style>
    .rendered-widgets {
        display: none;
    }

    .rendered-callouts {
        display: none;
    }
</style>