<script context="module">
    import {writable} from "svelte/store";

    const sliderVal = writable(0);

    export function resetParameters() {
        sliderVal.set(0);
    }

</script>

<script>
    import {getContext} from "svelte";
    import {timeFormat} from "d3-time-format";
    import moment from "moment";
    import _ from "lodash";

    const fmt = timeFormat("%Y-%m-%d");
    const selectedOverlay = getContext("selectedOverlay");
    const widgetParameters = getContext("widgetParameters");

    function onSelect(futureDate) {
        $widgetParameters = {
            targetDate: fmt(futureDate)
        };
        $selectedOverlay = null;
    }

    const debouncedOnSelect = _.debounce(onSelect, 500);

    let futureDate = null;

    $: futureDate = moment().set("date", 1).add($sliderVal, "months");
    $: debouncedOnSelect(futureDate);
</script>


<label for="future-date">Projected costs for:</label>
<span>{fmt(futureDate)}</span>

<input id="future-date"
       type="range"
       min="0"
       max="120"
       bind:value={$sliderVal}>

<div class="help-block">
    Use the slider to adjust how far in the future to project costs.
    This is calculated by incorporating app retirement dates and subtracting their associated
    costs from the current total.
</div>
