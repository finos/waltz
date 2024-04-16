<script>

    import Icon from "./Icon.svelte";
    import RatingIndicatorCell from "../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";

    export let ratingCharacteristics;
    export let hasDecorator = false;

</script>

<span>
    <span class="waltz-visibility-parent">
        <span class:waltz-visibility-child-30={!hasDecorator}>
            {#if ratingCharacteristics.sourceOutboundRating === 'NO_OPINION'}
                <span style={`color: ${ratingCharacteristics.sourceOutboundClassification.color}`}>
                    <Icon size="xl" name="square-o"/>
                </span>
            {:else}
                <RatingIndicatorCell {...ratingCharacteristics.sourceOutboundClassification} showName={false}/>
            {/if}
            <Icon style="vertical-align: middle"
                  name="long-arrow-right"
                  size="xl"/>
            {#if ratingCharacteristics.targetInboundRating === 'NO_OPINION'}
                <span style={`color: ${ratingCharacteristics.targetInboundClassification.color}`}>
                    <Icon size="xl" name="square-o"/>
                </span>
            {:else}
                <RatingIndicatorCell {...ratingCharacteristics.targetInboundClassification} showName={false}/>
            {/if}
        </span>
    </span>
</span>