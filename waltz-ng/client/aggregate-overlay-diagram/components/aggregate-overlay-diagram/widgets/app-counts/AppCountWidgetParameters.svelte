<script>
    import {aggregateOverlayDiagramStore} from "../../../../../svelte-stores/aggregate-overlay-diagram-store";
    import {getContext} from "svelte";
    import {timeFormat} from "d3-time-format";
    import moment from "moment";
    import BulkAppCountWidget from "./BulkAppCountWidget.svelte";
    import Icon from "../../../../../common/svelte/Icon.svelte";

    export let opts;

    const fmt = timeFormat("%Y-%m-%d");
    const overlayData = getContext("overlayData");
    const selectedDiagram = getContext("selectedDiagram");

    const widget = getContext("widget");
    let selectedDefinition;
    let overlayDataCall;
    let futureDate = null;
    let slideVal = 0;

    function onSelect(futureDate) {
        const dateStr = fmt(futureDate);
        overlayDataCall = aggregateOverlayDiagramStore.findAppCountsForDiagram(
            $selectedDiagram.id,
            opts,
            dateStr,
            true);
        $widget = BulkAppCountWidget;
    }

    const debouncedOnSelect = _.debounce(onSelect, 500);

    $: {
        $overlayData = $overlayDataCall?.data;
    }

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