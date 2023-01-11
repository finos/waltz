<script>

    import {defaultPermission, selectedAssessment, selectedRatingId} from "./rating-store";
    import NoData from "../../../common/svelte/NoData.svelte";
    import RatingIndicatorCell from "../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {cardinality} from "../../../common/services/enums/cardinality";


    export let onEdit;
    export let onAdd;

    function edit(rating) {
        $selectedRatingId = rating.rating.id;
        onEdit();
    }

    $: hasAddPermission = _.includes($defaultPermission?.operations, "ADD");

    $: allRatingsAssigned = _.size($selectedAssessment?.dropdownEntries) === _.size($selectedAssessment?.ratings)

    $: singleValueCanAdd = $selectedAssessment?.definition.cardinality === cardinality.ZERO_ONE.key && _.isEmpty($selectedAssessment?.ratings)
    $: multiValueCanAdd = $selectedAssessment?.definition.cardinality === cardinality.ZERO_MANY.key && !allRatingsAssigned

    $: canAdd = hasAddPermission && (singleValueCanAdd || multiValueCanAdd);

</script>


{#if $selectedAssessment}
    {#if $selectedAssessment.definition.cardinality === cardinality.ZERO_MANY.key}
        <h4>Ratings:</h4>
    {:else}
        <h4>Rating:</h4>
    {/if}
    <div class="help-block">
        <Icon name="info-circle"/>
        Click on a rating to view more detail and make edits
    </div>
    <ul class="list-unstyled">
        {#each $selectedAssessment?.ratings as rating}
            <li>
                <button class="btn btn-skinny"
                        on:click={() => edit(rating)}>
                    <RatingIndicatorCell {...rating.ratingItem}
                                         showName="true"
                                         showGroup="true"/>
                </button>
            </li>
        {:else }
            <NoData>There are no ratings for this assessment</NoData>
        {/each}

        {#if canAdd}
            <li>
                <button class="btn btn-skinny"
                        on:click={onAdd}>
                    <Icon name="plus"/>
                    Add
                </button>
            </li>
        {/if}
    </ul>
{/if}