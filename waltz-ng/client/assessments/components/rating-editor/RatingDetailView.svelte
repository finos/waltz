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
    import {assessmentRatingStore} from "../../../svelte-stores/assessment-rating-store";
    import _ from "lodash";
    import RatingIndicatorCell from "../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";
    import TextEditableField from "../../../common/svelte/TextEditableField.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import toasts from "../../../svelte-stores/toast-store";
    import {displayError} from "../../../common/error-utils";
    import {cardinality} from "../../../common/services/enums/cardinality";
    import EditableRatingValue from "./EditableRatingValue.svelte";
    import Markdown from "../../../common/svelte/Markdown.svelte";

    export let onCancel;
    export let onRemove;

    let assessmentRatingCall;

    let rating;
    let canEdit = false;
    let canRemove = false;
    let ratingDisablementReason = null;  // if set this will disable the rating edit functionality


    $: {
        if ($selectedRating) {

            rating = {
                ratingId: $selectedRating?.rating.ratingId,
                comment: $selectedRating?.rating.comment,
                userSelectable: $selectedRating?.ratingItem.userSelectable
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

    function onLock() {
        return assessmentRatingStore
            .lock($primaryEntityReference, $selectedAssessment.definition.id, $selectedRating?.rating.ratingId)
            .then(() => {
                assessmentRatingCall = assessmentRatingStore.findForEntityReference($primaryEntityReference, true);
                $assessmentRatings = $assessmentRatingCall?.data;
            })
            .then(() => toasts.success("Successfully locked rating"))
            .catch(e => displayError("Failed to lock rating", e));
    }

    function onUnlock() {
        return assessmentRatingStore
            .unlock($primaryEntityReference, $selectedAssessment.definition.id, $selectedRating?.rating.ratingId)
            .then(() => {
                assessmentRatingCall = assessmentRatingStore.findForEntityReference($primaryEntityReference, true);
                $assessmentRatings = $assessmentRatingCall?.data;
            })
            .then(() => toasts.success("Successfully unlocked rating"))
            .catch(e => displayError("Failed to unlock rating", e));
    }

    function saveComment(comment) {
        return assessmentRatingStore
            .updateComment($selectedRating.rating.id, comment)
            .then(() => {
                assessmentRatingCall = assessmentRatingStore.findForEntityReference($primaryEntityReference, true);
                $assessmentRatings = $assessmentRatingCall?.data;
                rating.comment = comment;
            })
            .then(() => toasts.success("Successfully updated comment"))
            .catch(e => displayError("Failed to update comment", e));
    }

    function saveRating(newRating) {
        return assessmentRatingStore
            .updateRating($selectedRating.rating.id, {newRatingId: newRating.id})
            .then(() => {
                assessmentRatingCall = assessmentRatingStore.findForEntityReference($primaryEntityReference, true);
                $assessmentRatings = $assessmentRatingCall?.data;
                rating = newRating;
            })
            .then(() => toasts.success("Successfully updated rating"))
            .catch(e => displayError("Failed to update rating", e));
    }


    $: permissionsForRating = _.get($permissionsByRatingId, $selectedRating?.rating.id, $defaultPermission);

    $: locked = _.get($selectedRating, ["rating", "isReadOnly"], false);

    $: canEdit = rating.userSelectable && _.includes(permissionsForRating?.operations, "UPDATE") && !locked;
    $: canRemove = rating.userSelectable && _.includes(permissionsForRating?.operations, "REMOVE") && !locked;
    $: canLock = _.includes(permissionsForRating?.operations, "LOCK") && !locked;
    $: canUnlock = _.includes(permissionsForRating?.operations, "LOCK") && locked;


    $: isMultiValuedAssessment = $selectedAssessment?.definition.cardinality === cardinality.ZERO_MANY.key
    $: {
          if (!canEdit) {
              if(rating.userSelectable) {
                  ratingDisablementReason = "You do not have permissions to edit this rating, though you may be able to remove it";
              } else {
                  ratingDisablementReason = "You do not have permissions to edit this rating."
              }
          } else if (isMultiValuedAssessment) {
              ratingDisablementReason = "Multi-valued ratings cannot be editied, please remove and then add the new rating";
          } else {
              ratingDisablementReason = null;
          }
    }

</script>

{#if $selectedRating}

    <div class="form-group">
        <label for="rating">
            Rating
        </label>
        <div id="rating">
            <EditableRatingValue ratingItem={$selectedRating.ratingItem}
                                 showGroup={true}
                                 disablementReason={ratingDisablementReason}
                                 onSave={saveRating}/>
            <div class="help-block">
                <Markdown text={$selectedRating.ratingItem.description}/>
            </div>
        </div>
    </div>


    <div class="form-group">
        <div id="comment">
            <TextEditableField text={rating.comment}
                               label="Comment"
                               mandatory={$selectedRating.ratingItem.requiresComment}
                               editable={canEdit}
                               onSave={saveComment}/>
        </div>
    </div>

    {#if $selectedRating?.rating.isReadOnly || !rating.userSelectable}
        <div class="help-block">
            <span style="color: orange">
                <Icon name="lock"/>
            </span>
            This rating is read only
        </div>
    {/if}


    <div class="form-group">
        <label for="last-update">
            Last Updated
        </label>
        <div id="last-update">
            <LastEdited entity={$selectedRating?.rating}/>
        </div>
    </div>

{/if}

<div style="padding: 1em 0">

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

    <button class="btn btn-skinny"
            on:click={onCancel}>
        <Icon name="times"/>
        Cancel
    </button>

</div>

