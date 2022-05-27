<script>

    import {getContext} from "svelte";
    import BackingEntitiesOverlayCell from "./BackingEntitiesOverlayCell.svelte";
    import {aggregateOverlayDiagramStore} from "../../../../../svelte-stores/aggregate-overlay-diagram-store";
    import Icon from "../../../../../common/svelte/Icon.svelte";

    const overlayData = getContext("overlayData");
    const selectedDiagram = getContext("selectedDiagram");
    const widget = getContext("widget");

    let overlayDataCall;

    $: {
        if ($selectedDiagram) {
            overlayDataCall = aggregateOverlayDiagramStore.findBackingEntitiesForDiagram($selectedDiagram.id);
            $widget = {
                overlay: BackingEntitiesOverlayCell
            };
        }
    }

    $: $overlayData = $overlayDataCall?.data;

</script>

{#if $overlayDataCall?.status === 'loading'}
    <h4>
        Loading
        <Icon name="refresh" spin="true"/>
    </h4>
{/if}