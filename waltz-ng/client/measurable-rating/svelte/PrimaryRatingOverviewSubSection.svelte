<script>

    import SubSection from "../../common/svelte/SubSection.svelte";
    import {measurableRatingStore} from "../../svelte-stores/measurable-rating-store";
    import {mkSelectionOptions} from "../../common/selector-utils";
    import _ from "lodash";
    import RatingIndicatorCell from "../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import Icon from "../../common/svelte/Icon.svelte";
    import {activeSections} from "../../dynamic-section/section-store";
    import {dynamicSections} from "../../dynamic-section/dynamic-section-definitions";
    import Tooltip from "../../common/svelte/Tooltip.svelte";
    import PrimaryRatingTooltipContent from "./PrimaryRatingTooltipContent.svelte";

    export let parentEntityRef;

    let primaryRatingsCall;
    let measurablesById = {};
    let categoriesById = {};
    let ratingsById = {};

    $: {
        if(parentEntityRef) {
            const opts = mkSelectionOptions(parentEntityRef);
            primaryRatingsCall = measurableRatingStore.getPrimaryRatingsViewBySelector(opts);
        }
    }

    $: primaryRatingsView = $primaryRatingsCall?.data;


    $: {
        if(primaryRatingsView){
            measurablesById = _.keyBy(primaryRatingsView.measurables, d => d.id);
            categoriesById = _.keyBy(primaryRatingsView.measurableCategories, d => d.id);
            ratingsById = _.keyBy(primaryRatingsView.ratingSchemeItems, d => d.id);
        }
    }

    $: ratingsRows = _
        .chain(primaryRatingsView?.measurableRatings || [])
        .map(d => {
            const measurable = measurablesById[d?.measurableId];
            const category = categoriesById[measurable?.categoryId];
            const ratingSchemeItem = ratingsById[d.ratingId];
            return {
                rating: d,
                measurable,
                category,
                ratingSchemeItem
            }
        })
        .orderBy(["category.position", "category.name"])
        .value();

    function mkRatingTooltipProps(row) {
        return {
            rating: row.rating,
            ratingSchemeItem: row.ratingSchemeItem,
            measurable: row.measurable
        }
    }

</script>

<SubSection>
    <div slot="header">
        Primary Viewpoint Ratings
    </div>
    <div slot="content">
        {#if !_.isEmpty(primaryRatingsView?.measurableRatings)}
            <table class="waltz-field-table  waltz-field-table-border" style="width: 100%">
                <tbody>
                {#each ratingsRows as rating}
                    <tr>
                        <td class="wft-label" style="padding: 3px; width: 30%">
                            {_.get(rating, ["category", "name"], "Unknown Category")}
                        </td>
                        <td style="padding: 3px 6px; width: 70%">
                            <Tooltip content={PrimaryRatingTooltipContent}
                                     placement="left-start"
                                     props={mkRatingTooltipProps(rating)}>
                                <svelte:fragment slot="target">
                                    <RatingIndicatorCell {...rating.ratingSchemeItem}
                                                         showName="false"/>
                                    {_.get(rating, ["measurable", "name"], "Unknown Measurable")}
                                </svelte:fragment>
                            </Tooltip>
                        </td>
                    </tr>
                {/each}
                </tbody>
            </table>
        {:else}
            <div class="help-block">
                <Icon name="info-circle"/> No primary ratings have been set for this application
            </div>
        {/if}
    </div>
    <div slot="controls">
        <span class="small pull-right">
            <button class="btn btn-skinny btn-sm"
                    on:click={() =>  activeSections.add(dynamicSections.measurableRatingAppSection)}>
                More
            </button>
        </span>
    </div>
</SubSection>