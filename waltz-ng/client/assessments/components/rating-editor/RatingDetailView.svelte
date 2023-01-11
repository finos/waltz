<script>

    import {
        assessmentRatings,
        assessments,
        defaultPermission,
        permissionsByRatingId,
        primaryEntityReference,
        selectedAssessment,
        selectedRating
    } from "./rating-store";
    import {assessmentRatingStore} from "../../../svelte-stores/assessment-rating";
    import _ from "lodash";
    import RatingIndicatorCell from "../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";
    import TextEditableField from "../../../common/svelte/TextEditableField.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    export let onCancel;
    export let onRemove;

    let assessmentRatingCall;

    let rating;


    $: {
        if ($selectedRating) {

            rating = {
                ratingId: $selectedRating?.rating.ratingId,
                comment: $selectedRating?.rating.comment
            }
        }
    }

    function onSave() {
        const seletedDefnId = $selectedAssessment?.definition.id;
        assessmentRatingStore
            .store($primaryEntityReference, seletedDefnId, rating)
            .then(() => {
                assessmentRatingCall = assessmentRatingStore.findForEntityReference($primaryEntityReference, true);
                $assessmentRatings = $assessmentRatingCall?.data;
                $selectedAssessment = _.find($assessments, d => d.definition.id === seletedDefnId)
            })
            .then(onCancel);
    }

    $: permissionsForRating = _.get($permissionsByRatingId, $selectedRating?.rating.id, $defaultPermission);

    $: locked = _.get($selectedRating, ["rating", "isReadOnly"], false);

    $: canEdit = _.includes(permissionsForRating?.operations, "UPDATE") && !locked;
    $: canRemove = _.includes(permissionsForRating?.operations, "REMOVE") && !locked;
    $: canLock = _.includes(permissionsForRating?.operations, "LOCK") && !locked;
    $: canUnlock = _.includes(permissionsForRating?.operations, "LOCK") && locked;


    function onLock() {
        return assessmentRatingStore
            .lock($primaryEntityReference, $selectedAssessment.definition.id, $selectedRating?.rating.ratingId)
            .then(() => {
                assessmentRatingCall = assessmentRatingStore.findForEntityReference($primaryEntityReference, true);
                $assessmentRatings = $assessmentRatingCall?.data;
            });
    }

    function onUnlock() {
        return assessmentRatingStore
            .unlock($primaryEntityReference, $selectedAssessment.definition.id, $selectedRating?.rating.ratingId)
            .then(() => {
                assessmentRatingCall = assessmentRatingStore.findForEntityReference($primaryEntityReference, true);
                $assessmentRatings = $assessmentRatingCall?.data;
            });
    }

    function saveComment(comment) {
        return assessmentRatingStore
            .update($selectedRating.rating.id, comment)
            .then(() => {
                assessmentRatingCall = assessmentRatingStore.findForEntityReference($primaryEntityReference, true);
                $assessmentRatings = $assessmentRatingCall?.data;
                rating.comment = comment;
            });
    }

</script>

<h4>Rating Detail:</h4>

{#if $selectedRating}
    <div class="form-group">
        <label for="rating">
            Rating
        </label>
        <div id="rating">
            <RatingIndicatorCell {...$selectedRating?.ratingItem}
                                 showName="true"
                                 showGroup="true"/>
        </div>
    </div>

    <div class="form-group">
        <label for="last-update">
            Last Updated
        </label>
        <div id="last-update">
            <LastEdited entity={$selectedRating?.rating}/>
        </div>
    </div>

    <div class="form-group">
        <label for="comment">
            Comment
        </label>
        <div id="comment">
            <TextEditableField text={rating.comment}
                               editable={canEdit}
                               onSave={saveComment}/>
        </div>
    </div>

    {#if $selectedRating?.rating.isReadOnly}
        <div class="help-block">
            <span style="color: orange">
                <Icon name="lock"/>
            </span>
            This rating is read only
        </div>
    {/if}

    {#if canLock}
        <button class="btn btn-skinny"
                on:click={onLock}>
            <Icon name="lock"/>
            Lock
        </button>
    {/if}

    {#if canUnlock}
        <button class="btn btn-skinny"
                on:click={onUnlock}>
            <Icon name="unlock"/>
            Unlock
        </button>
    {/if}

    {#if canRemove}
        <button class="btn btn-skinny"
                on:click={onRemove}>
            <Icon name="trash"/>
            Remove
        </button>
    {/if}
{/if}
<button class="btn btn-skinny"
        on:click={onCancel}>
    <Icon name="times"/>
    Cancel
</button>