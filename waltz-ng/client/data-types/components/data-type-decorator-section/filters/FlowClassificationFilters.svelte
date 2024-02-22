<script>

    import {
        FilterKinds,
        mkClassificationFilterId
    } from "../../../../data-flow/components/svelte/flow-detail-tab/filters/filter-utils";
    import _ from "lodash";
    import RatingIndicatorCell from "../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import {mkClassificationViewFilter} from "../../../../common/view-grid-utils";
    import {updateFilters} from "../data-type-decorator-section-store";
    import NoData from "../../../../common/svelte/NoData.svelte";

    export let classifications = [];
    export let filters;

    function selectClassification(classification) {

        const filterId = mkClassificationFilterId();

        const existingFilter = _.find($filters, f => f.id === filterId);

        const existingClassifications = _.get(existingFilter, "classifications", []);

        const newClassifications = _.some(existingClassifications, r => _.isEqual(r, classification.code))
            ? _.filter(existingClassifications, d => !_.isEqual(d, classification.code))
            : _.concat(existingClassifications, [classification.code]);

        const newFilter = mkClassificationViewFilter(filterId, newClassifications)

        updateFilters(filters, filterId, newFilter);
    }


    function isSelected(filters, fcCode){
        const classificationFilter = _.find(
            filters,
            f => f.id === mkClassificationFilterId());
        return _.some(
            _.get(classificationFilter, "classifications", []),
            selectedFcCode => fcCode === selectedFcCode);
    }

    function onClearFilters() {
        $filters = _.reject($filters, f => f.kind === FilterKinds.FLOW_CLASSIFICATION);
    }

    $: classificationFilter = _.find($filters, d => d.kind === FilterKinds.FLOW_CLASSIFICATION);
    $: hasFilters = !_.isEmpty(_.get(classificationFilter, ["classifications"], []));

</script>

<div class="help-block"
 style="padding-top: 1em;">
    Filters the flows based upon their classification ratings.
</div>
<div style="display: flex; padding-top: 1em;  padding-bottom: 1em">
    <table class="table table-condensed table small table-hover">
        <thead>
        <tr>
            <th>
                Flow Classification
            </th>
            <th>
                {#if hasFilters}
                    <button class="btn-skinny"
                            style="font-weight: lighter"
                            on:click={onClearFilters}>
                        Clear
                    </button>
                {/if}
            </th>
        </tr>
        </thead>
        <tbody>
        {#each _.orderBy(classifications, d => d.name) as fc}
        <tr class="clickable"
            class:selected={isSelected($filters, fc.code)}
            on:click={() => selectClassification(fc)}>
            <td>
                <RatingIndicatorCell {...fc}/>
            </td>
            <td>
                {fc.description}
            </td>
        </tr>
        {:else}
            <tr>
                <td colspan="2">
                    <NoData type="info">There are no classifications to filter over</NoData>
                </td>
            </tr>
        {/each}
        </tbody>
    </table>
</div>

<style>

    .selected {
        background-color: #eefaee !important;
    }

</style>