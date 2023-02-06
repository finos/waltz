<script>

    import {defaultPermission, selectedAssessment, selectedRatingId} from "./rating-store";
    import NoData from "../../../common/svelte/NoData.svelte";
    import RatingIndicatorCell from "../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {cardinality} from "../../../common/services/enums/cardinality";
    import {truncate} from "../../../common/string-utils";


    export let onEdit;
    export let onAdd;

    let canAdd = false;

    function edit(rating) {
        $selectedRatingId = rating.rating.id;
        onEdit();
    }

    $: hasAddPermission = _.includes($defaultPermission?.operations, "ADD");

    $: allRatingsAssigned = _.size($selectedAssessment?.dropdownEntries) === _.size($selectedAssessment?.ratings)

    $: singleValueCanAdd = $selectedAssessment?.definition.cardinality === cardinality.ZERO_ONE.key && _.isEmpty($selectedAssessment?.ratings)
    $: multiValueCanAdd = $selectedAssessment?.definition.cardinality === cardinality.ZERO_MANY.key && !allRatingsAssigned

    $: canAdd = hasAddPermission && (singleValueCanAdd || multiValueCanAdd);

    $: sortedRatingList =_.sortBy($selectedAssessment?.ratings, d => _.toLower(d.ratingItem.ratingGroup + d.ratingItem.name));

</script>


{#if $selectedAssessment}
    <div class="help-block">
        <Icon name="info-circle"/>
        Click on a rating to view more detail and make edits
    </div>
    <table class="table table-condensed table-hover">
        <tbody>
            {#each sortedRatingList as rating}
            <tr class="clickable"
                on:click={() => edit(rating)}>
                <td>
                    <button class="btn btn-skinny">
                        <RatingIndicatorCell {...rating.ratingItem}
                                             showName="true"
                                             showGroup="true"/>
                    </button>
                </td>
                <td>
                    <div class="small">
                        {truncate(rating.rating.comment, 20)}
                    </div>
                </td>
            </tr>
            {:else }
                <tr>
                    <td colspan="2">
                        <NoData>There are no ratings for this assessment</NoData>
                    </td>
                </tr>
            {/each}

            {#if canAdd}
                <tr on:click={onAdd}
                    class="clickable">
                    <td colspan="2">
                        <button class="btn btn-skinny">
                            <Icon name="plus"/>
                            Add
                        </button>
                    </td>
                </tr>
            {/if}

        </tbody>
    </table>
{/if}
