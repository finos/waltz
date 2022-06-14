<script>
    import _ from "lodash";
    import {getContext} from "svelte";
    import {widgets} from "../aggregate-overlay-diagram-utils";
    import NoData from "../../../../common/svelte/NoData.svelte";

    const focusWidget = getContext("focusWidget");
    const filterParameters = getContext("filterParameters");
    const widgetParameters = getContext("widgetParameters");
    const diagramPresets = getContext("diagramPresets");
    const selectedPreset = getContext("selectedPreset");

    function selectPreset(preset) {

        //must set to null until the parameters are prepared to make the new data call
        $focusWidget = null;
        $selectedPreset = preset;

        const overlayConfig = JSON.parse(preset.overlayConfig);
        const filterConfig = JSON.parse(preset.filterConfig);

        const selectedWidget = _.find(widgets, d => d.key === overlayConfig.widgetKey);
        const selectedFilter = _.first(filterConfig);

        $widgetParameters = overlayConfig.widgetParameters;
        $filterParameters = selectedFilter.filterParameters;

        $focusWidget = selectedWidget;
    }

</script>


{#if _.isEmpty($diagramPresets)}
    <NoData type="warning">There are no presets for this diagram</NoData>
{:else}
    <table class="table table-condensed table-hover">
        <colgroup>
            <col width="40%"/>
            <col width="60%"/>
        </colgroup>
        <thead>
        <tr>
            <th>Presets</th>
            <th></th>
        </tr>
        </thead>
        <tbody>
        {#each $diagramPresets as preset}
            <tr class="clickable"
                class:selectedPreset={$selectedPreset?.id === preset.id}
                on:click={() => selectPreset(preset)}>
                <td>
                    <button class="btn btn-skinny">{preset.name}</button>
                </td>
                <td>
                    {preset.description || "-"}
                </td>
            </tr>
        {/each}
        </tbody>
    </table>
{/if}


<style>

    .selectedPreset {
        background-color: #fffbdc;
    }

</style>