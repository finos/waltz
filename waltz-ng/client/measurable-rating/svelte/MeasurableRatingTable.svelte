<script>

    import _ from "lodash";
    import {termSearch} from "../../common";
    import SearchInput from "../../common/svelte/SearchInput.svelte";
    import RatingIndicatorCell from "../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import NoData from "../../common/svelte/NoData.svelte";
    import Icon from "../../common/svelte/Icon.svelte";
    import {truncate, truncateMiddle} from "../../common/string-utils";
    import Tooltip from "../../common/svelte/Tooltip.svelte";
    import ReplacementAppMiniTable from "./ReplacementAppMiniTable.svelte";
    import {selectedMeasurable} from "../components/panel/measurable-rating-panel-store";
    import AssessmentRatingsMiniTable from "./AssessmentRatingsMiniTable.svelte";
    import MeasurableRatingViewIconTooltip from "./MeasurableRatingViewIconTooltip.svelte";

    export let category;
    export let ratings = [];
    export let measurables = [];
    export let allocationSchemes = [];
    export let allocations = [];
    export let assessmentDefinitions = [];
    export let assessmentRatings = [];
    export let plannedDecommissions = [];
    export let replacementApps = [];
    export let measurableHierarchyById;
    export let onSelect = (d) => console.log("selecting", d)

    let qry;

    $: assessmentRatingsByEntityId = _.groupBy(assessmentRatings, d => d.entityReference.id);
    $: allocationsByRatingId = _.groupBy(allocations, d => d.measurableRatingId);
    $: measurablesById = _.keyBy(measurables, d => d.id);
    $: decommsByRatingId = _.keyBy(plannedDecommissions, d => d.measurableRatingId);
    $: replacementAppsByDecommId = _.groupBy(replacementApps, d => d.decommissionId);

    $: allocationsBySchemeId = _.keyBy(allocations, d => d.schemeId);
    $: measurablesById = _.keyBy(measurables, d => d.id);

    $: enrichedMeasurables = _
        .chain(ratings)
        .map(d => {

            const assessments = _.get(assessmentRatingsByEntityId, [d.id], []);
            const assessmentsByDefId = _.groupBy(assessments, d => d.assessmentDefinitionId);

            const allocations = _.get(allocationsByRatingId, [d.id], []);
            const allocationsBySchemeId = _.keyBy(allocations, d => d.schemeId);

            const measurable = measurablesById[d.measurableId];
            const hierarchy = _.get(measurableHierarchyById, [measurable?.id, "parents"], []);

            const path = _
                .chain(hierarchy)
                .orderBy(d => d.level)
                .map(d => d.parentReference.name)
                .join(" / ")
                .value();

            const decommission = decommsByRatingId[d.id];
            const replacementApps = _.get(replacementAppsByDecommId, decommission?.id, []);

            const replacementAppString = _
                .chain(replacementApps)
                .map(d => d.entityReference.name)
                .join(", ")
                .value();

            const assessmentOutcomeString = _.chain(assessments)
                .map(d => d.ratingSchemeItem.name)
                .join(", ")
                .value();

            return Object.assign(
                {},
                {
                    rating: d,
                    measurable,
                    hierarchyPath: path,
                    allocations,
                    assessmentsByDefId,
                    allocationsBySchemeId,
                    decommission,
                    replacementApps,
                    replacementAppString,
                    assessmentOutcomeString
                })
        })
        .orderBy(d => d.measurable.name)
        .value();

    $: ratingsList = _.isEmpty(qry)
        ? enrichedMeasurables
        : termSearch(
            enrichedMeasurables,
            qry,
            [
                "rating.rating",
                "measurable.externalId",
                "measurable.name",
                "rating.ratingSchemeItem.name",
                "hierarchyPath",
                "replacementAppString",
                "assessmentOutcomeString"
            ]);

    function mkTooltipProps(rating) {
        return {
            rating: rating.rating,
            plannedDecommission: rating.decommission,
            replacementApps: rating.replacementApps,
            allocations: rating.allocations
        }
    }

</script>



<h4>
    Ratings for Category
    <span class="small">
        (
        {#if _.size(ratingsList) !== _.size(ratings)}
            {_.size(ratingsList)} /
        {/if}
        {_.size(ratings)}
        )
    </span>
</h4>

<div>
    <SearchInput bind:value={qry}/>
</div>

<div class="table-container"
     class:waltz-scroll-region-350={_.size(ratings) > 10}>
    <table class="table table-condensed small table-hover"
           style="margin-top: 1em">
        <thead>
        <tr>
            <th nowrap="nowrap"></th>
            <th nowrap="nowrap" style="width: 20em">Taxonomy Item</th>
            <th nowrap="nowrap" style="width: 20em">Taxonomy Item Ext ID</th>
            <th nowrap="nowrap" style="width: 20em">Rating</th>
            <th nowrap="nowrap" style="width: 20em">Taxonomy Path</th>
            {#each allocationSchemes as scheme}
                <th nowrap="nowrap" style="width: 20em">{scheme.name}</th>
            {/each}
            {#if !_.isEmpty(plannedDecommissions)}
                <th nowrap="nowrap" style="width: 20em">Planned Decommission Date</th>
            {/if}
            {#if !_.isEmpty(replacementApps)}
                <th nowrap="nowrap" style="width: 20em">Replacement Applications</th>
            {/if}
            {#each assessmentDefinitions as defn}
                <th nowrap="nowrap" style="width: 20em">{defn.name}</th>
            {/each}
        </tr>
        </thead>
        <tbody>
        {#each ratingsList as rating}
            <tr class="clickable"
                class:selected={$selectedMeasurable?.rating?.id === rating.rating.id}
                on:click={() => onSelect(rating)}>
                <td>
                    <Tooltip content={MeasurableRatingViewIconTooltip}
                             trigger={"mouseenter"}
                             props={mkTooltipProps(rating)}>
                        <svelte:fragment slot="target">
                            {#if rating.rating.isPrimary}
                                <Icon name="star"/>
                            {:else}
                                <Icon name="fw"/>
                            {/if}
                            {#if !_.isEmpty(rating.decommission)}
                                {#if !_.isEmpty(rating.replacementApps)}
                                    <Icon name="hand-o-right"/>
                                {:else}
                                    <Icon name="hand-paper-o"/>
                                {/if}
                            {:else}
                                <Icon name="fw"/>
                            {/if}
                            {#if !_.isEmpty(rating.allocations)}
                                <Icon name="pie-chart"/>
                            {:else}
                                <Icon name="fw"/>
                            {/if}
                            {#if rating.rating.isReadOnly}
                                <Icon name="lock"/>
                            {:else}
                                <Icon name="fw"/>
                            {/if}
                        </svelte:fragment>
                    </Tooltip>
                </td>
                <td>
                    {_.get(rating, ["measurable", "name"], "Unknown")}
                </td>
                <td>
                    <span title={_.get(rating, ["measurable", "externalId"], null)}>
                        {truncateMiddle(_.get(rating, ["measurable", "externalId"], "Unknown"), 30)}
                    </span>
                </td>
                <td>
                    <RatingIndicatorCell {...rating.rating.ratingSchemeItem}/>
                </td>
                <td>
                    <span title={_.get(rating, ["hierarchyPath"], null)}>
                        {truncateMiddle(_.get(rating, ["hierarchyPath"], "-"), 30)}
                    </span>
                </td>
                {#each allocationSchemes as scheme}
                    {@const allocation = _.get(rating.allocationsBySchemeId, [scheme.id])}
                    <td>
                        <span class:not-provided={_.isEmpty(allocation)}>
                            {_.get(allocation, ["percentage"], "Unallocated")}
                        </span>
                    </td>
                {/each}
                {#if !_.isEmpty(plannedDecommissions)}
                    <td>
                        {_.get(rating, ["decommission", "plannedDecommissionDate"], "")}
                    </td>
                {/if}
                {#if !_.isEmpty(replacementApps)}
                    <td>
                        <Tooltip content={ReplacementAppMiniTable}
                                 trigger={"mouseenter"}
                                 props={{replacementApps: rating.replacementApps}}>
                            <svelte:fragment slot="target">
                                <span>{truncate(rating.replacementAppString, 30)}</span>
                            </svelte:fragment>
                        </Tooltip>
                    </td>
                {/if}
                {#each assessmentDefinitions as defn}
                    {@const assessmentRatingsForDefn = _.get(rating.assessmentsByDefId, defn.id, [])}
                    <td>
                        <Tooltip content={AssessmentRatingsMiniTable}
                                  trigger={"mouseenter"}
                                  props={{assessmentRatings: assessmentRatingsForDefn}}>
                            <svelte:fragment slot="target">
                                <div class="rating-col">
                                    {#each _.take(assessmentRatingsForDefn, 3) as assessment}
                                        <RatingIndicatorCell {...assessment.ratingSchemeItem}/>
                                    {/each}
                                    {#if _.size(assessmentRatingsForDefn) > 3}
                                        <span>...</span>
                                    {/if}
                                </div>
                            </svelte:fragment>
                        </Tooltip>
                    </td>
                {/each}
            </tr>
        {:else}
            <tr>
                <td colspan={5 + _.size(assessmentDefinitions) + _.size(allocationSchemes) + (category.allowPrimaryRatings ? 1 : 0)}>
                    <NoData type="info">There are no ratings to show, these may have been filtered.</NoData>
                </td>
            </tr>
        {/each}
        </tbody>
    </table>
</div>


<style>

    .selected {
        background: #eefaee !important;
    }

    table {
        display: table;
        white-space: nowrap;
        position: relative;
        border-collapse: separate;
    }

    th {
        position: sticky;
        top: 0;
        background: white;
        z-index: 1;
    }

    .table-container {
        overflow-x: auto;
        padding-top: 0;
    }

    .rating-col {
        display: flex;
        gap: 1em;
    }

    .not-provided {
        font-style: italic;
    }
</style>