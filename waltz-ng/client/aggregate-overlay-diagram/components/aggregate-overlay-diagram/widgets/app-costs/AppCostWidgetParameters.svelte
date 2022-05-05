<script>
    import {aggregateOverlayDiagramStore} from "../../../../../svelte-stores/aggregate-overlay-diagram-store";
    import {getContext} from "svelte";
    import Icon from "../../../../../common/svelte/Icon.svelte";
    import BulkAppCostWidget from "./BulkAppCostWidget.svelte";

    export let opts;

    const overlayData = getContext("overlayData");
    const selectedDiagram = getContext("selectedDiagram");

    const widget = getContext("widget");
    let selectedDefinition;
    let overlayDataCall;

    function onSelect() {
        overlayDataCall = aggregateOverlayDiagramStore.findAppCostForDiagram(
            $selectedDiagram.id,
            opts,
            true);
        $widget = BulkAppCostWidget;
    }

    $: {
        $overlayData = $overlayDataCall?.data;
    }


</script>

<div class="help-block">
    Blah blah
</div>

<button class="btn btn-success"
        on:click={onSelect}>
    Load data
</button>


{#if $overlayDataCall?.status === 'loading'}
    <h4>
        Loading
        <Icon name="refresh" spin="true"/>
    </h4>
{/if}