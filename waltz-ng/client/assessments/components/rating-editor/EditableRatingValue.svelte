<script>

    import Icon from "../../../common/svelte/Icon.svelte";
    import SavingPlaceholder from "../../../common/svelte/SavingPlaceholder.svelte";
    import RatingIndicatorCell from "../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import _ from "lodash";
    import {selectedAssessment} from "./rating-store";
    import RatingItemDropdownPicker from "./RatingItemDropdownPicker.svelte";

    export let ratingItem;
    export let showGroup = false;
    export let disablementReason = null;
    export let onSave = (rating) => console.log("Rating to save", {rating});

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT",
        SAVING: "SAVING"
    }

    let activeMode = Modes.VIEW
    let savePromise;

    function save(rating) {
        activeMode = Modes.SAVING;

        savePromise = onSave(rating);

        return savePromise
            .then(() => activeMode = Modes.VIEW);
    }

    function selectRating(evt) {
        return save(evt.detail);
    }

    $: existingRatings = _.map($selectedAssessment.ratings, d => d.rating.ratingId);
    $: availableRatings = _.filter($selectedAssessment.dropdownEntries, d => !_.includes(existingRatings, d.id));

    $: isDisabled = disablementReason !== null || _.isEmpty(availableRatings);
    $: disablementMessage = disablementReason
        || (_.isEmpty(availableRatings)
                ? "There are no other ratings to choose from"
                : null);
</script>


{#if activeMode === Modes.VIEW}
    <RatingIndicatorCell {...ratingItem}
                         showName="true"
                         showGroup={showGroup}/>
    <button class="btn btn-skinny"
            title={disablementMessage}
            disabled={isDisabled}
            on:click={() => activeMode = Modes.EDIT}>
        <Icon name="pencil"/>
        Edit
    </button>
{:else if activeMode === Modes.EDIT}

    <RatingItemDropdownPicker ratings={availableRatings}
                              selectedRatingId={ratingItem.id}
                              on:select={selectRating}/>

    <button class="btn btn-skinny"
            on:click={save}>
        <Icon name="floppy-o"/>
        Save
    </button>
    <button class="btn btn-skinny"
            on:click={() => activeMode = Modes.VIEW}>
        <Icon name="times"/>
        Cancel
    </button>
{:else if activeMode === Modes.SAVING}
    <SavingPlaceholder/>
{/if}