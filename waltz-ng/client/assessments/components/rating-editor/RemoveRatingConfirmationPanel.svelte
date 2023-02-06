<script>

    import {assessmentRatingStore} from "../../../svelte-stores/assessment-rating-store";
    import {assessmentRatings, primaryEntityReference, selectedAssessment, selectedRating} from "./rating-store";
    import RatingIndicatorCell from "../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import toasts from "../../../svelte-stores/toast-store";
    import {displayError} from "../../../common/error-utils";

    export let onCancel;

    let assessmentRatingCall;

    function remove() {
        const selectedDefnId = $selectedAssessment.definition.id;
        assessmentRatingStore
            .remove($primaryEntityReference, selectedDefnId, $selectedRating.rating.ratingId)
            .then(() => {
                assessmentRatingCall = assessmentRatingStore.findForEntityReference($primaryEntityReference, true);
                return $assessmentRatings = $assessmentRatingCall?.data;
            })
            .then(onCancel)
            .then(() => toasts.success("Successfully removed rating"))
            .catch(e => displayError("Failed to remove rating", e));
    }

</script>

<div class="removal-box">
    <h4>Removal Confirmation:</h4>
    <p>
        Are you sure you want to remove the rating:
        <br>
        <br>
        <RatingIndicatorCell {...$selectedRating?.ratingItem}
                             showName="true"
                             showGroup="true"/>
        <br>
        <br>
        from the assessment <strong>{$selectedAssessment.definition.name}</strong>?
    </p>

    <button class="btn btn-skinny"
            on:click={remove}>
        <Icon name="trash"/>
        Remove
    </button>
    <button class="btn btn-skinny"
            on:click={onCancel}>
        <Icon name="times"/>
        Cancel
    </button>

</div>

<style>
    .removal-box{
        border-width: 1px;
        border-style: solid;
        border-color: #d93f44;
        background-color: #fae9ee;
        padding-left: 2em;
        padding-right: 2em;
        padding-bottom: 1.5em;
        padding-top: 1.5em;
        border-radius: 2px;
    }
</style>