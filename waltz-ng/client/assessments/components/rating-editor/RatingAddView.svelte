<script>
    import _ from "lodash";
    import {assessmentRatings, primaryEntityReference, selectedAssessment} from "./rating-store";
    import {assessmentRatingStore} from "../../../svelte-stores/assessment-rating-store";
    import Icon from "../../../common/svelte/Icon.svelte";
    import toasts from "../../../svelte-stores/toast-store";
    import {displayError} from "../../../common/error-utils";
    import Markdown from "../../../common/svelte/Markdown.svelte";

    export let onCancel;

    const newRating = {
        ratingId: null,
        comment: null
    };

    let dropdownConfig;
    let assessmentRatingCall;

    function onSave() {
        const seletedDefnId = $selectedAssessment.definition.id;
        assessmentRatingStore
            .store($primaryEntityReference, seletedDefnId, newRating)
            .then(() => {
                assessmentRatingCall = assessmentRatingStore.findForEntityReference($primaryEntityReference, true);
                return $assessmentRatings = $assessmentRatingCall?.data;
            })
            .then(onCancel)
            .then(() => toasts.success("Successfully added rating"))
            .catch(e => displayError("Failed to add rating", e));
    }

    $: {
        const existingRatings = _.map($selectedAssessment.ratings, d => d.rating.ratingId);

        const grouped = _
            .chain($selectedAssessment.dropdownEntries)
            .filter(d => !_.includes(existingRatings, d.id))
            .sortBy([d => d.ratingGroup, d => d.position, d => d.name])
            .groupBy("ratingGroup")
            .value();

        if (grouped[null] && _.size(grouped) > 1) {
            grouped["Ungrouped"] = grouped[null];
            delete grouped[null];
        }

        dropdownConfig = grouped[null]
            ? {style: "simple", options: grouped[null]}
            : {style: "grouped", groups: _.map(grouped, (v, k) => ({groupName: k, options: v}))};
    }

    $: selectedRating = _.find(
        $selectedAssessment.dropdownEntries,
        d => d.id === newRating.ratingId);
</script>

<h4>Add New Rating:</h4>

<form on:submit|preventDefault={onSave}>
    <div class="form-group">
        <label for="rating-dropdown">
            Rating
        </label>
        <select id="rating-dropdown"
                class="form-control"
                bind:value={newRating.ratingId}>
            {#if dropdownConfig.style === 'simple'}
                {#each dropdownConfig.options as entry}
                    <option value={entry.id}>
                        {entry.name}
                    </option>
                {/each}
            {:else if dropdownConfig.style === 'grouped'}
                {#each dropdownConfig.groups as g}
                    <optgroup label={g.groupName}>
                        {#each g.options as entry}
                            <option value={entry.id}>
                                {entry.name}
                            </option>
                        {/each}
                    </optgroup>
                {/each}
            {/if}
        </select>
        <div class="help-block">
            {#if selectedRating}
                <Markdown text={selectedRating.description}/>
            {:else}
                Please select a rating
            {/if}
        </div>
    </div>

    <div class="form-group">
        <label for="comment">
            Comment {#if selectedRating?.requiresComment}<span class="required-indicator">*</span>{/if}
        </label>
        <textarea id="comment"
                  class="form-control"
                  rows="6"
                  required={selectedRating?.requiresComment}
                  placeholder="This comment supports markdown"
                  bind:value={newRating.comment}></textarea>
        {#if selectedRating?.requiresComment}
            <div class="help-block">
                A comment is mandatory for this rating
            </div>
        {/if}
    </div>

    <button type="submit"
            class="btn btn-skinny">
        <Icon name="floppy-o"/>
        Save
    </button>

    <button class="btn btn-skinny"
            on:click={onCancel}>
        <Icon name="times"/>
        Cancel
    </button>

</form>

<style>
    .required-indicator {
        color: red;
    }
</style>