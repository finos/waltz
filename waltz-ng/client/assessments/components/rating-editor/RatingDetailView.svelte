<script>

    import {
        assessmentRatings,
        assessments,
        primaryEntityReference,
        selectedAssessment,
        selectedRating
    } from "./rating-store";
    import {assessmentRatingStore} from "../../../svelte-stores/assessment-rating";
    import _ from "lodash";
    import RatingIndicatorCell from "../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";

    export let onCancel;

    let assessmentRatingCall;

    let rating = {
        ratingId: $selectedRating.rating.ratingId,
        comment: $selectedRating.rating.comment
    }

    function onSave() {
        const seletedDefnId = $selectedAssessment.definition.id;
        assessmentRatingStore
            .store($primaryEntityReference, seletedDefnId, rating)
            .then(() => {
                assessmentRatingCall = assessmentRatingStore.findForEntityReference($primaryEntityReference, true);
                $assessmentRatings = $assessmentRatingCall?.data;
                $selectedAssessment = _.find($assessments, d => d.definition.id === seletedDefnId)
            })
            .then(onCancel);
    }

</script>

<h4>Detail view of rating</h4>

<div class="form-group">
    <label for="rating">
        Rating
    </label>
    <div id="rating">
        <RatingIndicatorCell {...$selectedRating.ratingItem}
                             show-name="true"/>
    </div>
</div>

<div class="form-group">
    <label for="last-update">
        Last Updated
    </label>
    <div id="last-update">
        <LastEdited entity={$selectedRating.rating}/>
    </div>
</div>

<div class="form-group">
    <label for="comment">
        Comment
    </label>
    <textarea id="comment"
              class="form-control"
              rows="6"
              placeholder="This comment supports markdown"
              bind:value={rating.comment}></textarea>
</div>

<button class="btn btn-skinny"
        on:click={onSave}>
    Save
</button>
<button class="btn btn-skinny"
        on:click={onCancel}>
    Cancel
</button>