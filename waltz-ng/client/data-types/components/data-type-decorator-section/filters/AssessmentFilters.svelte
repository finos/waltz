<script>

    import RatingIndicatorCell from "../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import NoData from "../../../../common/svelte/NoData.svelte";
    import EntityIcon from "../../../../common/svelte/EntityIcon.svelte";
    import {mkDefinitionFilterId} from "../../../../data-flow/components/svelte/flow-detail-tab/filters/filter-utils";
    import {updateFilters} from "../data-type-decorator-section-store";
    import {mkAssessmentViewFilter} from "../../../../common/view-grid-utils";
    import _ from "lodash";

    export let assessmentFilters = [];
    export let filters;

    function selectRating(definitionId, ratingId) {

        const filterId = mkDefinitionFilterId(definitionId);

        const existingFilter = _.find($filters, f => f.id === filterId);

        const ratingInfo = {
            definitionId,
            ratingId
        };

        const existingRatings = _.get(existingFilter, "ratings", []);

        const newRatings = _.some(existingRatings, r => _.isEqual(r, ratingInfo))
            ? _.filter(existingRatings, d => !_.isEqual(d, ratingInfo))
            : _.concat(existingRatings, [ratingInfo]);

        const newFilter = mkAssessmentViewFilter(filterId, newRatings, d => d.assessmentRatings)

        updateFilters(filters, filterId, newFilter);

    }

    function clearFiltersForDefinition(defnId) {
        const filterId = mkDefinitionFilterId(defnId);
        $filters = _.reject($filters, d => d.id === filterId);
    }

    function isSelected(filters, defnId, ratingId){
        const ratingInfo = { definitionId: defnId, ratingId: ratingId};
        const filter = _.find(filters, d => d.id === mkDefinitionFilterId(defnId));
        const filteredRatings = _.get(filter, "ratings",  []);
        return _.some(filteredRatings, r => _.isEqual(r, ratingInfo))
    }

</script>

<div class="help-block"
 style="padding-top: 1em">
    Use the assessment ratings to filter the logical flows. Only ratings aligned to a flow can be filtered upon.
</div>
<div style="display: flex; gap: 1em;">
    {#each assessmentFilters as assessment}
        <div style="flex: 1 1 30%">
            <table class="table table-condensed table small table-hover">
                <thead>
                <tr>
                    <th>
                        <EntityIcon kind={assessment?.definition?.entityKind}/>
                        {assessment?.definition?.name}
                        <button class="btn-skinny"
                                style="font-weight: lighter"
                                on:click={() => clearFiltersForDefinition(assessment?.definition.id)}>
                            Clear
                        </button>
                    </th>
                </tr>
                </thead>
                <tbody>
                {#each assessment?.ratings as rating}
                    <tr class="clickable"
                        class:selected={isSelected($filters, assessment?.definition.id, rating.id)}
                        on:click={() => selectRating(assessment.definition.id, rating.id)}>
                        <td>
                            <RatingIndicatorCell {...rating}/>
                        </td>
                    </tr>
                {/each}
                </tbody>
            </table>
        </div>
    {:else}
        <NoData type="info">No flows have been given a rating for a primary assessment</NoData>
    {/each}
</div>

<style>

    .selected {
        background-color: #eefaee !important;
    }

</style>