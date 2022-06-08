<script>

    import {getContext} from "svelte";
    import AggregatedEntitiesOverlayCell from "./AggregatedEntitiesOverlayCell.svelte";
    import {aggregateOverlayDiagramStore} from "../../../../../svelte-stores/aggregate-overlay-diagram-store";
    import Icon from "../../../../../common/svelte/Icon.svelte";
    import _ from "lodash";

    const overlayDataCall = getContext("overlayDataCall");
    const overlayData = getContext("overlayData");
    const widget = getContext("widget");
    const remoteMethod = getContext("remoteMethod");
    const widgetParameters = getContext("widgetParameters");
    const selectedOverlay = getContext("selectedOverlay");

    function mkGlobalProps(data) {
        const maxCount = _
            .chain(data)
            .map(d => _.size(d.aggregatedEntityReferences))
            .max()
            .value();
        return {maxCount};
    }

    $: {

        $remoteMethod = aggregateOverlayDiagramStore.findAggregatedEntitiesForDiagram;

        $widgetParameters = {};

        $selectedOverlay = null;

        $widget = {
            overlay: AggregatedEntitiesOverlayCell,
            mkGlobalProps
        };
    }
</script>

{#if $overlayDataCall?.status === 'loading'}
    <h4>
        Loading
        <Icon name="refresh" spin="true"/>
    </h4>
{/if}