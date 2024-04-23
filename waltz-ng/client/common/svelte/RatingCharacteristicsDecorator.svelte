<script>

    import FlowRatingCell from "./FlowRatingCell.svelte";

    export let ratingCharacteristics;
    export let hasDecorator = false;

</script>

<div style="display: inline-block"
     class="waltz-visibility-parent">
    <div class:waltz-visibility-child-30={!hasDecorator}>
        <FlowRatingCell targetInboundClassification={ratingCharacteristics.targetInboundClassification}
                        sourceOutboundClassification={ratingCharacteristics.sourceOutboundClassification}/>
    </div>
</div>
