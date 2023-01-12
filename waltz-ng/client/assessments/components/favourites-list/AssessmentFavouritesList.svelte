<script>

    import _ from "lodash";
    import {userPreferenceStore} from "../../../svelte-stores/user-preference-store";
    import {onMount} from "svelte";
    import {assessmentStores, createStores,} from "../list/assessment-rating-store";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {assessmentDefinitionStore} from "../../../svelte-stores/assessment-definition";
    import {assessmentRatingStore} from "../../../svelte-stores/assessment-rating-store";
    import {ratingSchemeStore} from "../../../svelte-stores/rating-schemes";
    import {
        assessmentDefinitions,
        assessmentRatings,
        assessments,
        primaryEntityReference,
        ratingSchemes
    } from "../rating-editor/rating-store";
    import Tooltip from "../../../common/svelte/Tooltip.svelte";
    import AssessmentRatingTooltipContent from "../list/AssessmentRatingTooltipContent.svelte";
    import RatingIndicatorCell from "../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import AssessmentDefinitionTooltipContent from "../list/AssessmentDefinitionTooltipContent.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";


    let elem;
    let stores = null;
    let defaultPrimaryList;
    let favouriteIncludedIds;
    let favouriteExcludedIds;
    let favouriteIds;
    let setFromPreferences;
    let userPreferences = null;
    let userPreferenceCall;
    let assessmentsWithoutRatings;
    let assessmentsWithRatings;

    onMount(() => {
        userPreferenceCall = userPreferenceStore.findAllForUser();
    });

    let assessmentDefinitionCall;
    let assessmentRatingCall;
    let ratingSchemesCall;

    $: {
        if ($primaryEntityReference) {
            assessmentDefinitionCall = assessmentDefinitionStore.findByEntityReference($primaryEntityReference);
            assessmentRatingCall = assessmentRatingStore.findForEntityReference($primaryEntityReference, true);
            ratingSchemesCall = ratingSchemeStore.loadAll();
        }
    }

    $: $assessmentDefinitions = $assessmentDefinitionCall?.data;
    $: $assessmentRatings = $assessmentRatingCall?.data;
    $: $ratingSchemes = $ratingSchemesCall?.data;

    $: {
        if ($primaryEntityReference && _.isNil($assessmentStores)) {
            $assessmentStores = createStores($primaryEntityReference.kind);
        }

        defaultPrimaryList = $assessmentStores?.defaultPrimaryList;
        favouriteIncludedIds = $assessmentStores?.favouriteIncludedIds;
        favouriteExcludedIds = $assessmentStores?.favouriteExcludedIds;
        favouriteIds = $assessmentStores?.favouriteIds;
        setFromPreferences = $assessmentStores?.setFromPreferences;
    }

    $: {
        if ($userPreferenceCall?.status === "loaded") {
            userPreferences = $userPreferenceCall?.data;
        }
    }


    $: {
        if (userPreferences && $assessmentStores) {
            $assessmentStores.setFromPreferences(userPreferences);
        }
    }

    $: favouriteAssessments = _
        .chain($assessments)
        .filter(a => _.includes($favouriteIds, a.definition.id))
        .value() || [];

    $: {
        if ($assessments) {

            const valuePartitioned = _.partition(
                favouriteAssessments,
                assessment => _.isEmpty(assessment.ratings));

            assessmentsWithoutRatings = _.sortBy(valuePartitioned[0], d => d.definition.name);
            assessmentsWithRatings = _.sortBy(valuePartitioned[1], d => d.definition.name);
        }
    }

    function mkRatingTooltipProps(row) {
        return {
            rating: row.rating,
            ratingItem: row.ratingItem
        }
    }

    function mkDefinitionTooltipProps(row) {
        return {
            definition: row.definition
        }
    }

</script>

{#if _.isEmpty(favouriteAssessments)}
    <NoData type="info">You have no favourite assessments, please open the assessments sectiont to add some</NoData>
{:else}
    <table class="table table-hover table-condensed">
        <colgroup>
            <col width="50%"/>
            <col width="50%"/>
        </colgroup>
        <tbody>
        {#each assessmentsWithRatings as item}
            <tr>
                <td>
                    <Tooltip content={AssessmentDefinitionTooltipContent}
                             placement="left-start"
                             props={mkDefinitionTooltipProps(item)}>
                        <svelte:fragment slot="target">
                            {item.definition.name}
                            <Icon name={item.definition.isReadOnly ? "lock" : "fw"}/>
                        </svelte:fragment>
                    </Tooltip>
                </td>
                <td>
                    <ul class="list-unstyled">
                        {#each item.ratings as rating}
                            <li>
                                <Tooltip content={AssessmentRatingTooltipContent}
                                         placement="left-start"
                                         props={mkRatingTooltipProps(rating)}>
                                    <svelte:fragment slot="target">
                                        <RatingIndicatorCell {...rating.ratingItem}
                                                             showName="true"/>
                                        {#if rating.rating.comment}
                                            <Icon name="sticky-note-o"/>
                                        {/if}
                                    </svelte:fragment>
                                </Tooltip>
                            </li>
                        {/each}
                    </ul>
                </td>
            </tr>
        {:else}
            <tr>
                <td colspan="2">
                    <NoData type="info">There are no favourite assessments with ratings</NoData>
                </td>
            </tr>
        {/each}
        {#if !_.isEmpty(assessmentsWithoutRatings)}
            <tr style="vertical-align: top">
                <td>
                    <span class="wft-label">
                        Not Provided
                    </span>
                </td>
                <td>
                    <ul class="text-muted list-inline">
                        {#each assessmentsWithoutRatings as item}
                            <li>
                                <waltz-icon ng-if="item.definition.isReadOnly"
                                            name="lock">
                                </waltz-icon>
                                {item.definition.name}
                                {#if _.indexOf(assessmentsWithoutRatings, item) !== _.size(assessmentsWithoutRatings) - 1}
                                    ,
                                {/if}
                            </li>
                        {/each}
                    </ul>
                </td>
            </tr>
        {/if}
        </tbody>
    </table>
{/if}
