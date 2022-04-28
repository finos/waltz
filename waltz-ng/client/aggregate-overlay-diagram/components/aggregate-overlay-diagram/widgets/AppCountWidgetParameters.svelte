<script>
    import {aggregateOverlayDiagramStore} from "../../../../svelte-stores/aggregate-overlay-diagram-store";
    import {getContext} from "svelte";
    import DatePicker from "../../../../common/svelte/DatePicker.svelte";
    import {timeFormat} from "d3-time-format";
    import BulkAppCountWidget from "./BulkAppCountWidget.svelte";

    export let opts;

    const fmt = timeFormat("%Y-%m-%d");
    const overlayData = getContext("overlayData");
    const selectedDiagram = getContext("selectedDiagram");

    const widget = getContext("widget");
    let selectedDefinition;
    let overlayDataCall;
    let futureDate = null;

    function onSelect() {
        const dateStr = fmt(futureDate);
        overlayDataCall = aggregateOverlayDiagramStore.findAppCountsForDiagram($selectedDiagram.id, opts, dateStr);
        $widget = BulkAppCountWidget;
    }

    $: {
        $overlayData = $overlayDataCall?.data;
    }

    function onDateChange(evt) {
        futureDate = evt.detail;
        onSelect();
    }
</script>

<DatePicker on:change={onDateChange}
            canEdit={true}/>
<button on:click={onSelect}>Get Data</button>