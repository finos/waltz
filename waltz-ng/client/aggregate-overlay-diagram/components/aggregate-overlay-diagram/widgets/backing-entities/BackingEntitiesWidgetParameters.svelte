<script>

    import {getContext} from "svelte";
    import BackingEntitiesOverlayCell from "./BackingEntitiesOverlayCell.svelte";
    import {aggregateOverlayDiagramStore} from "../../../../../svelte-stores/aggregate-overlay-diagram-store";
    import _ from "lodash";
    import BackingEntitiesWidget from "./BackingEntitiesWidget.svelte";
    import Icon from "../../../../../common/svelte/Icon.svelte";

    const overlayData = getContext("overlayData");
    const selectedDiagram = getContext("selectedDiagram");
    const widget = getContext("widget");

    let overlayDataCall;

    $: {
        if ($selectedDiagram) {
            overlayDataCall = aggregateOverlayDiagramStore.findBackingEntitiesForDiagram($selectedDiagram.id);
            $widget = BackingEntitiesWidget;
        }
    }

    $: $overlayData = $overlayDataCall?.data;

</script>


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