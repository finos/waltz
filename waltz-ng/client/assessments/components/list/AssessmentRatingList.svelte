<script>

    import _ from "lodash";
    import {userPreferenceStore} from "../../../svelte-stores/user-preference-store";
    import {onMount} from "svelte";
    import {
        assessmentStores,
        createStores,
    } from "./assessment-rating-store";
    import AssessmentRatingListGroup from "./AssessmentRatingListGroup.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../../common";
    import {assessmentDefinitionStore} from "../../../svelte-stores/assessment-definition";
    import {assessmentRatingStore} from "../../../svelte-stores/assessment-rating-store";
    import {ratingSchemeStore} from "../../../svelte-stores/rating-schemes";
    import {
        assessmentDefinitions,
        assessmentRatings,
        assessments,
        ratingSchemes,
        selectedAssessmentId,
        primaryEntityReference,
        detailPanelActiveMode,
        Modes
    } from "../rating-editor/rating-store";


    let elem;
    let stores = null;
    let defaultPrimaryList;
    let favouriteIncludedIds;
    let favouriteExcludedIds;
    let favouriteIds;
    let setFromPreferences;
    let expansions;
    let userPreferences = null;
    let userPreferenceCall;
    let qry;
    let groupedAssessments;

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

    function toggleGroup(group) {
        expansions = _.includes(expansions, group.groupName)
            ? _.without(expansions, group.groupName)
            : _.concat(expansions, group.groupName);
    }


    function selectAssessment(evt) {
        $detailPanelActiveMode = Modes.LIST
        $selectedAssessmentId = evt.detail.definition.id;
    }


    function toggleFavourite(row) {

        const isExplicitlyIncluded = _.includes($favouriteIncludedIds, row.definition.id);
        const isExplicitlyExcluded = _.includes($favouriteExcludedIds, row.definition.id);
        const isDefault = _.includes($defaultPrimaryList, row.definition.id);

        let message;

        if (isExplicitlyIncluded) {
            message = "Removing from favourite assessments"
            $favouriteIncludedIds = _.without($favouriteIncludedIds, row.definition.id);
        } else if (isExplicitlyExcluded) {
            message = "Adding to favourite assessments"
            $favouriteExcludedIds = _.without($favouriteExcludedIds, row.definition.id);
        } else if (isDefault) {
            message = "Removing from favourite assessments"
            $favouriteExcludedIds = _.concat($favouriteExcludedIds, row.definition.id);
        } else {
            message = "Adding to favourite assessments"
            $favouriteIncludedIds = _.concat($favouriteIncludedIds, row.definition.id);
        }
    }


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
        // before loaded defaults to initial state [], the derived stores pick this up and reset the favourites
        if ($userPreferenceCall?.status === "loaded") {
            userPreferences = $userPreferenceCall?.data;
        }
    }


    $: {
        if (userPreferences && $assessmentStores) {
            $assessmentStores.setFromPreferences(userPreferences);
        }
    }


    $: {
        if ($assessmentStores) {
            expansions = _
                .chain($assessments)
                .filter(d => _.includes($favouriteIncludedIds, d.definition.id)
                    || _.includes($defaultPrimaryList, d.definition.id))
                .reject(d => _.includes($favouriteExcludedIds, d.definition.id))
                .map(d => d.definition.definitionGroup)
                .uniq()
                .value();
        }
    }

    $: visibleAssessments = _.isEmpty(qry)
        ? $assessments
        : termSearch($assessments, qry, ["definition.name"]);

    $: groupedAssessments = _
        .chain(visibleAssessments)
        .groupBy(d => d.definition?.definitionGroup)
        .map((v, k) => {

            const [notProvided, provided] = _
                .chain(v)
                .orderBy(d => d.definition.name)
                .partition(d => _.isEmpty(d.ratings))
                .value()

            return {
                groupName: k,
                notProvided,
                provided
            }
        })
        .orderBy([d => d.groupName === "Uncategorized", d => d.groupName])
        .value();

</script>


<div class="row">

    <div class="col-sm-12">
        <SearchInput bind:value={qry}/>
        <table class="table table-hover table-condensed">
            <colgroup>
                <col width="10%"/>
                <col width="50%"/>
                <col width="40%"/>
            </colgroup>
            {#each groupedAssessments as group}
                <tbody class="assessment-group">
                <tr class="assessment-group-header clickable">
                    <td>
                        {#if _.includes(expansions, group.groupName)}
                            <button class="btn btn-skinny"
                                    data-ux={`${group.groupName}-caret-down-button`}
                                    on:click={() => toggleGroup(group)}>
                                <Icon size="lg"
                                      name={"caret-down"}/>
                            </button>
                        {:else}
                            <button class="btn btn-skinny"
                                    data-ux={`${group.groupName}-caret-right-button`}
                                    on:click={() => toggleGroup(group)}>
                                <Icon size="lg"
                                      name={"caret-right"}/>
                            </button>
                        {/if}
                    </td>
                    <td colspan="2"
                        class="clickable">
                        <button class="btn btn-skinny"
                                on:click={() => toggleGroup(group)}>
                            <strong>
                                <span>{group.groupName}</span>
                            </strong>
                        </button>
                    </td>
                </tr>
                {#if _.includes(expansions, group.groupName)}
                    <AssessmentRatingListGroup group={group}
                                               on:select={selectAssessment}
                                               toggleFavourite={toggleFavourite}
                                               favouriteIds={favouriteIds}/>
                {/if}
                </tbody>
            {/each}
        </table>
    </div>

</div>


<style>

    .assessment-group-header {
        background-color: #eee;
    }

</style>

