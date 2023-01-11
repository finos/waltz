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