<script>
    import {aggregateOverlayDiagramStore} from "../../../../../svelte-stores/aggregate-overlay-diagram-store";
    import {getContext} from "svelte";
    import {timeFormat} from "d3-time-format";
    import moment from "moment";
    import Icon from "../../../../../common/svelte/Icon.svelte";
    import TargetAppCostOverlayCell from "./TargetAppCostOverlayCell.svelte";
    import _ from "lodash";

    const fmt = timeFormat("%Y-%m-%d");
    const selectedOverlay = getContext("selectedOverlay");
    const costSliderValue = getContext("costSliderValue");
    const remoteMethod = getContext("remoteMethod");
    const widgetParameters = getContext("widgetParameters");
    const overlayDataCall = getContext("overlayDataCall");
    const widget = getContext("widget");


    function mkGlobalProps(data) {
        const maxCost = _
            .chain(data)
            .map(d => [d.currentStateCost, d.targetStateCost])
            .flatten()
            .max()
            .value();
        return { maxCost };
    }

    function onSelect(futureDate) {

        $remoteMethod = aggregateOverlayDiagramStore.findTargetAppCostForDiagram;

        $widgetParameters = {
            targetDate: fmt(futureDate)
        }

        $selectedOverlay = null;

        $widget = {
            overlay: TargetAppCostOverlayCell,
            mkGlobalProps
        };
    }

    const debouncedOnSelect = _.debounce(onSelect, 500);

    $: futureDate = moment().set("date", 1).add($costSliderValue * 2, "months");
    $: debouncedOnSelect(futureDate);

</script>


<label for="future-date">Projected costs for:</label>
<span>{fmt(futureDate)}</span>

<input id="future-date"
       type="range"
       min="0"
       max="60"
       bind:value={$costSliderValue}>

<div class="help-block">
    Use the slider to adjust how far in the future to project costs.
    This is calculated by incorporating app retirement dates and subtracting their associated
    costs from the current total.
</div>


{#if $overlayDataCall?.status === 'loading'}
    <h4>
        Loading
        <Icon name="refresh" spin="true"/>
    </h4>
{/if}