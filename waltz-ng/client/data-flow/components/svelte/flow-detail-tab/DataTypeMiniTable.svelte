<script context="module">
    import {writable} from "svelte/store";
    let hideRatings = writable(true);
</script>

<script>
    import _ from "lodash";
    import EntityLabel from "../../../../common/svelte/EntityLabel.svelte";
    import Toggle from "../../../../common/svelte/Toggle.svelte";
    import FlowRatingCell from "../../../../common/svelte/FlowRatingCell.svelte";
    import {flowDirection as FlowDirection} from "../../../../common/services/enums/flow-direction";
    import RatingIndicatorCell from "../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";

    export let decorators = [];
    export let flowClassifications = []

    let inboundClassifications;
    let outboundClassifications;

    $: [inboundClassifications, outboundClassifications] = _.partition(flowClassifications, d => d.direction === FlowDirection.INBOUND.key);
    $: inboundClassificationsByCode = _.keyBy(inboundClassifications, d => d.code);
    $: outboundClassificationsByCode = _.keyBy(outboundClassifications, d => d.code);

    $: hasRatings =  !_.isEmpty(flowClassifications)
    $: showRatings = !$hideRatings && hasRatings;

</script>

<table class="">
    {#each _.orderBy(decorators, d => d.decoratorEntity.name) as type}
        <tbody class="small">
        <tr>
            <td colspan="2">
                <FlowRatingCell targetInboundClassification={inboundClassificationsByCode[type.targetInboundRating]}
                                sourceOutboundClassification={outboundClassificationsByCode[type.rating]}/>
                <EntityLabel ref={type.decoratorEntity}
                             showIcon={false}>
                </EntityLabel>
            </td>
        </tr>
        {#if showRatings}
            <tr>
                <td class="inline-label">
                    Producer Rating
                </td>
                <td>
                    <RatingIndicatorCell {..._.get(outboundClassificationsByCode, [type.rating])}
                                         showName="true"/>
                </td>
            </tr>
            <tr>
                <td class="inline-label">
                    Consumer Rating
                </td>
                <td>
                    <RatingIndicatorCell {..._.get(inboundClassificationsByCode, [type.targetInboundRating])}
                                         showName="true"/>
                </td>
            </tr>
        {/if}
        </tbody>
    {/each}
</table>

{#if hasRatings}
    <div class="smaller">
        <Toggle state={showRatings}
                labelOn="Hide Ratings"
                onToggle={() => $hideRatings = !$hideRatings}
                labelOff="Show Ratings"/>
    </div>
{/if}



<style>

    table {
        width: 100%;
    }

    table td {
        padding-bottom: 0.2em;
        padding-right: 0.6em;
        border-bottom: 1px solid #eee;
        vertical-align: top;
    }

    .inline-label {
        color: #999;
    }
</style>