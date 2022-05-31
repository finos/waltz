<script>
    import {aggregateOverlayDiagramStore} from "../../../../../svelte-stores/aggregate-overlay-diagram-store";
    import {getContext} from "svelte";
    import {timeFormat} from "d3-time-format";
    import Icon from "../../../../../common/svelte/Icon.svelte";
    import AppCountOverlayCell from "./AppCountOverlayCell.svelte";
    import _ from "lodash";

    const fmt = timeFormat("%Y-%m-%d");
    const selectedDiagram = getContext("selectedDiagram");
    const selectedOverlay = getContext("selectedOverlay");
    const widget = getContext("widget");
    const appCountSliderValue = getContext("appCountSliderValue");
    const filterParameters = getContext("appCountSliderValue");
    const widgetParameters = getContext("widgetParameters");
    const remoteMethod = getContext("remoteMethod");
    const overlayDataCall = getContext("overlayDataCall");

    let futureDate = null;

    function mkGlobalProps(data) {
        const maxCount = _
            .chain(data)
            .map(d => [d.currentStateCount, d.targetStateCount])
            .flatten()
            .max()
            .value();
        return { maxCount };
    }

    function onSelect(futureDate) {

        $remoteMethod = aggregateOverlayDiagramStore.findAppCountsForDiagram;

        $widgetParameters = {
            targetDate: fmt(futureDate)
        }

        $selectedOverlay = null;

        $widget = {
            overlay: AppCountOverlayCell,
            mkGlobalProps
        };
    }

    const debouncedOnSelect = _.debounce(onSelect, 500);


    $: debouncedOnSelect(futureDate);

</script>



<label for="future-date">Projected app counts for:</label>
<span>{fmt(futureDate)}</span>

<input id="future-date"
       type="range"
       min="0"
       max="60"
       bind:value={$appCountSliderValue}>

<div class="help-block">
    Use the slider to adjust how far in the future to application counts.
    This is calculated by incorporating app retirement dates and subtracting their associated
    apps from the current total.
</div>


{#if $overlayDataCall?.status === 'loading'}
    <h4>
        Loading
        <Icon name="refresh" spin="true"/>
    </h4>
{/if}