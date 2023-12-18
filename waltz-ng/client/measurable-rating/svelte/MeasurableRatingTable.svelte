<script>

    import _ from "lodash";
    import {termSearch} from "../../common";
    import SearchInput from "../../common/svelte/SearchInput.svelte";
    import RatingIndicatorCell from "../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import NoData from "../../common/svelte/NoData.svelte";
    import Icon from "../../common/svelte/Icon.svelte";
    import {truncate} from "../../common/string-utils";
    import Tooltip from "../../common/svelte/Tooltip.svelte";
    import ReplacementAppMiniTable from "./ReplacementAppMiniTable.svelte";
    import {selectedMeasurable} from "../components/panel/measurable-rating-panel-store";

    export let ratings = [];
    export let measurables = [];
    export let allocationSchemes = [];
    export let allocations = [];
    export let assessmentDefinitions = [];
    export let assessmentRatings = [];
    export let plannedDecommissions = [];
    export let replacementApps = [];
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

            const decommission = decommsByRatingId[d.id];
            const replacements = _.get(replacementAppsByDecommId, decommission?.id, []);

            const replacementAppString = _
                .chain(replacements)
                .map(d => d.entityReference.name)
                .join(", ")
                .value();

            return Object.assign(
                {},
                {
                    rating: d,
                    measurable,
                    allocations,
                    assessmentsByDefId,
                    allocationsBySchemeId,
                    decommission,
                    replacements,
                    replacementAppString
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
                "rating.ratingSchemeItem.name"
            ]);


    $: console.log({selected: $selectedMeasurable, plannedDecommissions, replacementApps});

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
            <th nowrap="nowrap" style="width: 20em">Measurable</th>
            <th nowrap="nowrap" style="width: 20em">Measurable Ext ID</th>
            <th nowrap="nowrap" style="width: 20em">Rating</th>
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
                class:selected={$selectedMeasurable?.rating.id === rating.rating.id}
                on:click={() => onSelect(rating)}>
                <td>
                    {#if rating.rating.isPrimary}
                        <Icon name="star" title="This is the primary rating"/>
                    {/if}
                </td>
                <td>
                    {_.get(rating, ["measurable", "name"], "Unknown")}
                </td>
                <td>
                    {_.get(rating, ["measurable", "externalId"], "Unknown")}
                </td>
                <td>
                    <RatingIndicatorCell {...rating.rating.ratingSchemeItem}/>
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
                                 props={{replacementApps}}>
                            <svelte:fragment slot="target">
                                <span>{truncate(rating.replacementAppString, 30)}</span>
                            </svelte:fragment>
                        </Tooltip>
                    </td>
                {/if}
                {#each assessmentDefinitions as defn}
                    {@const assessmentRatingsForDefn = _.get(rating.assessmentsByDefId, defn.id, [])}
                    <td>
                        <div class="rating-col">
                            {#each assessmentRatingsForDefn as assessment}
                                <RatingIndicatorCell {...assessment.ratingSchemeItem}/>
                            {/each}
                        </div>
                    </td>
                {/each}
            </tr>
        {:else}
            <tr>
                <td colspan={5 + _.size(assessmentDefinitions)}>
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