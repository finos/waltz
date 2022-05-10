<script>
    import {aggregateOverlayDiagramStore} from "../../../../../svelte-stores/aggregate-overlay-diagram-store";
    import {getContext} from "svelte";
    import {timeFormat} from "d3-time-format";
    import BulkTargetAppCostWidget from "./BulkTargetAppCostWidget.svelte";
    import moment from "moment";
    import Icon from "../../../../../common/svelte/Icon.svelte";

    export let opts;

    const fmt = timeFormat("%Y-%m-%d");
    const overlayData = getContext("overlayData");
    const selectedDiagram = getContext("selectedDiagram");

    const widget = getContext("widget");
    let selectedDefinition;
    let overlayDataCall;

    function onSelect(futureDate) {
        const dateStr = fmt(futureDate);
        overlayDataCall = aggregateOverlayDiagramStore.findTargetAppCostForDiagram(
            $selectedDiagram.id,
            opts,
            dateStr,
            true);
        $widget = BulkTargetAppCostWidget;
    }

    $: {
        $overlayData = $overlayDataCall?.data;
    }

    let slideVal = 0;

    const debouncedOnSelect = _.debounce(onSelect, 500);

    $: futureDate = moment().set("date", 1).add(slideVal * 2, "months");
    $: debouncedOnSelect(futureDate);

</script>


<label for="future-date">Projected costs for:</label>
<span>{fmt(futureDate)}</span>

<input id="future-date"
       type="range"
       min="0"
       max="60"
       bind:value={slideVal}>

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