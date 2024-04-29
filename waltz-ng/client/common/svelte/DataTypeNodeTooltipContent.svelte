<script>

    import Icon from "./Icon.svelte";
    import RatingIndicatorCell from "../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import _ from "lodash";
    import DescriptionFade from "./DescriptionFade.svelte";
    import NoData from "./NoData.svelte";
    import {severity} from "../services/enums/severity";

    export let name;
    export let description;
    export let concrete;
    export let ratingCharacteristics;
    export let usageCharacteristics;
    export let isEditMode = false;

    $: inboundMessage = ratingCharacteristics?.inboundMessage
        || ratingCharacteristics?.targetInboundClassification.defaultMessage

    $: outboundMessage = ratingCharacteristics?.outboundMessage
        || ratingCharacteristics?.sourceOutboundClassification.defaultMessage

    $: inboundSeverity = ratingCharacteristics?.inboundMessage
        ? ratingCharacteristics?.inboundMessageSeverity
        : ratingCharacteristics?.targetInboundClassification.messageSeverity;

    $: outboundSeverity = ratingCharacteristics?.outboundMessage
        ? ratingCharacteristics?.outboundMessageSeverity
        : ratingCharacteristics?.sourceOutboundClassification.messageSeverity

</script>


<div class="row">
    <div class="col-sm-12">
        <h4>{name}</h4>
    </div>
</div>
<div class="row">
    <div class="col-sm-12">
        <DescriptionFade text={description}/>
    </div>
</div>
{#if !_.isEmpty(ratingCharacteristics)}
    <hr>
{/if}
{#if ratingCharacteristics}
    <div class="row">
        <div class="col-sm-4">Producer Rating:</div>
        <div class="col-sm-8">
            <RatingIndicatorCell {...ratingCharacteristics.sourceOutboundClassification} showName={true}/>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-12 help-block small">
            This indicates the rating of the flow according whether this source entity is authorised to distribute this data type
        </div>
    </div>
    {#if !_.isEmpty(outboundMessage)}
        <div class="row">
            <div class="col-sm-12">
                <NoData type={_.lowerCase(_.get(severity, [outboundSeverity, "name"], "info"))}>{outboundMessage}</NoData>
            </div>
        </div>
    {/if}
    <div class="row">
        <div class="col-sm-4">Consumer Rating:</div>
        <div class="col-sm-8">
            <RatingIndicatorCell {...ratingCharacteristics.targetInboundClassification} showName={true}/>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-12 help-block small">
            This rating expresses whether the target entity has a preference for or against this type of data being sent to it
        </div>
    </div>
    {#if !_.isEmpty(inboundMessage)}
        <div class="row">
            <div class="col-sm-12">
                <NoData type={_.lowerCase(_.get(severity, [inboundSeverity, "name"], "info"))}>{inboundMessage}</NoData>
            </div>
        </div>
    {/if}
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
