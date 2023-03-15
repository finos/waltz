<script>

    import _ from "lodash";
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
    import {favouriteAssessmentDefinitionStore} from "../../../svelte-stores/favourite-assessment-definition-store";


    let elem;
    let stores = null;
    let favouriteIds;
    let expansions = [];
    let qry;
    let groupedAssessments;

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

    $: $assessmentDefinitions = $assessmentDefinitionCall?.data || [];
    $: $assessmentRatings = $assessmentRatingCall?.data || [];
    $: $ratingSchemes = $ratingSchemesCall?.data || [];

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
        if (_.includes(favouriteIds, row.definition.id)) {
            favouriteAssessmentDefinitionStore.remove(row.definition.id);
        } else {
            favouriteAssessmentDefinitionStore.add(row.definition.id);
        }
    }


    $: expansions = _.isEmpty(expansions)
            ? _
            .chain($favouriteAssessmentDefinitionStore[$primaryEntityReference?.kind])
                .map(d => d.definitionGroup)
                .uniq()
                .value()
            : expansions;

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

    $: favouriteIds = _.map($favouriteAssessmentDefinitionStore[$primaryEntityReference?.kind], d => d.id);
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

