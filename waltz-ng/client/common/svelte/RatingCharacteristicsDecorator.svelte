<script>

    import Icon from "./Icon.svelte";
    import RatingIndicatorCell from "../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";

    export let ratingCharacteristics;
    export let hasDecorator = false;

</script>

<span>
    <span class="waltz-visibility-parent">
        <span class:waltz-visibility-child-30={!hasDecorator}>
            <RatingIndicatorCell {...ratingCharacteristics.sourceOutboundClassification} showName={false}/>
            <Icon style="vertical-align: middle"
                  name="long-arrow-right"
                  size="xl"/>
            <RatingIndicatorCell {...ratingCharacteristics.targetInboundClassification} showName={false}/>
        </span>
    </span>
</span>