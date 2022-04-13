<script>
    import _ from "lodash";
    import WidgetSelector from "./WidgetSelector.svelte";

    export let svg = "";

    let svgHolderElem;
    let dataProvider;
    let renderedWidgetRefs = {}; // this gets populated by the calls to `bind:this`
    let widgetComponent;

    function handleWidgetChange(e) {
        widgetComponent = e.detail.widget;
        dataProvider = e.detail.dataProvider;
    }

    $: console.log({data: $dataProvider})
    $: cellDataByCellExtId = _.keyBy($dataProvider?.data, d => d.cellExternalId);

    $: {
        if (svgHolderElem && renderedWidgetRefs) {
            _.each(renderedWidgetRefs, (v, k) =>{
                if (! v) return;
                console.log({v, k})
                const targetStatsBox = svgHolderElem.querySelector(`[data-cell-id=${k}] .statistics-box`);

                if (!targetStatsBox) {
                    console.log("Cannot find stats box for cell-id", k);
                    return;
                }

                const widgetRef = v.querySelector(".widget");

                if (!widgetRef) {
                    console.log("Cannot find widget section for copying into the stats box for cell-id", k);
                    return;
                }

                widgetRef.setAttribute("width", targetStatsBox.getAttribute("data-widget-width"));
                widgetRef.setAttribute("height", targetStatsBox.getAttribute("data-widget-height"));

                const existingWidget = targetStatsBox.querySelector(".widget");
                if (existingWidget) {
                    targetStatsBox.replaceChild(widgetRef.cloneNode(true), existingWidget);
                } else {
                    targetStatsBox.append(widgetRef);
                }
            });
        }
    }

</script>

<div class="row">
    <div class="col-md-9">
        <div bind:this={svgHolderElem}>
            {@html svg}
        </div>
    </div>
    <div class="col-md-3">
        <WidgetSelector on:change={handleWidgetChange}/>
    </div>
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

<style>
    .rendered-widgets {
        display: none;
    }
</style>