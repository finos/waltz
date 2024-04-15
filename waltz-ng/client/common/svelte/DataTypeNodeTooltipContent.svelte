<script>

    import Icon from "./Icon.svelte";
    import RatingIndicatorCell from "../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import _ from "lodash";

    export let name;
    export let description;
    export let concrete;
    export let ratingCharacteristics;
    export let usageCharacteristics;
    export let isEditMode = false;

</script>


<div class="row">
    <div class="col-sm-12">
        <h4>{name}</h4>
    </div>
</div>
<div class="row">
    <div class="col-sm-12">
        {description}
    </div>
</div>
{#if !_.isEmpty(ratingCharacteristics)}
    <hr>
{/if}
{#if ratingCharacteristics}
    <div class="row">
        <div class="col-sm-4">Source Outbound Rating:</div>
        <div class="col-sm-8">
            <RatingIndicatorCell {...ratingCharacteristics.sourceOutboundClassification} showName={true}/>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-12 help-block small">
            This describes the rating of the data flow looking at outbound flow classification rules from the upstream entity.
        </div>
    </div>
    <div class="row">
        <div class="col-sm-4">Target Inbound Rating:</div>
        <div class="col-sm-8">
            <RatingIndicatorCell {...ratingCharacteristics.targetInboundClassification} showName={true}/>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-12 help-block small">
            This describes the rating of the data flow looking at inbound flow classification rules for the downstream target entity.
        </div>
    </div>
{/if}
{#if !_.isEmpty(usageCharacteristics)}
    <hr>
{/if}
{#if !concrete && !_.isEmpty(usageCharacteristics)}
    <div class="row">
        <div class="col-sm-12">
            <span class="waltz-error-icon">
                <Icon name="exclamation-triangle"
                      style="vertical-align: middle"
                      size="xl"/>
            </span>
            This data type is non-concrete so should not be mapped to
        </div>
    </div>
{/if}
{#if isEditMode && !_.isEmpty(usageCharacteristics?.warningMessageForEditors)}
    <div class="row">
        <div class="col-sm-12">
            <span class="waltz-warning-icon">
                <Icon name="exclamation-triangle"
                      style="vertical-align: middle"
                      size="xl"/>
            </span>
            {usageCharacteristics.warningMessageForEditors}
        </div>
    </div>
{/if}
{#if !isEditMode && !_.isEmpty(usageCharacteristics?.warningMessageForViewers)}
    <div class="row">
        <div class="col-sm-12">
            <span class="waltz-warning-icon">
                <Icon name="exclamation-triangle"
                      style="vertical-align: middle"
                      size="xl"/>
            </span>
            {usageCharacteristics.warningMessageForViewers}
        </div>
    </div>
{/if}
