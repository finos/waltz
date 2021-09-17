<script>

    import _ from "lodash";
    import {assessmentRatingFilter, filteredClients} from "./flow-decorator-store";
    import {assessmentRatingViewStore} from "../../../svelte-stores/assessment-rating-view-service";
    import Icon from "../../../common/svelte/Icon.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {createEventDispatcher} from "svelte";
    import {entity} from "../../../common/services/enums/entity";

    const Modes = {
        DEFINITION: "DEFINITION",
        RATING: "RATING"
    }

    const dispatch = createEventDispatcher();

    let assessmentKind = 'APPLICATION';
    let selectedDefinition = null;
    let selectedRating = null;
    let activeMode = Modes.DEFINITION;

    $: entityIds = _
        .chain($filteredClients)
        .filter(c => c.kind === assessmentKind)
        .map(c => c.id)
        .value();

    $: groupedAssessmentCall = assessmentRatingViewStore.findEntitiesGroupedByDefinitionAndOutcome(assessmentKind, entityIds, true);
    $: groupedAssessments = _.orderBy($groupedAssessmentCall?.data, d => d.assessmentDefinition.name) || [];

    function selectKind(kind) {
        assessmentKind = kind;
        activeMode = Modes.DEFINITION
    }

    function selectDefinition(assessmentDefAndRatings) {
        selectedDefinition = assessmentDefAndRatings;
        activeMode = Modes.RATING;
    }

    function selectRating(rating) {
        if(selectedRating === rating){
            selectedRating = null;
            $assessmentRatingFilter = () => true;
        } else {
            selectedRating = rating;
            const entityIds = _.map(rating.entityReferences, d => d.id);
            $assessmentRatingFilter = d => assessmentKind === d.kind && _.includes(entityIds, d.id);
        }
    }

    function back() {
        selectedDefinition = null;
        selectedRating = null;
        $assessmentRatingFilter = null;
        activeMode = Modes.DEFINITION
    }

    function changeView() {
        dispatch('submit');
    }

</script>

<div class="row">
    <div class="col-sm-12">
        <div class="help-block">
            <Icon name="info-circle"/> Use the selection below to filter counterpart entities by assessment rating
        </div>
    </div>
    {#if activeMode === Modes.DEFINITION}
        <div  class="col-sm-12">
            <div>
                Select an assessment definition for applications:
            </div>
            {#if !_.isEmpty(groupedAssessments)}
                <div>
                    <div class="waltz-scroll-region-250">
                        <table class="table table-condensed table-hover small">
                            <thead>
                            <th></th>
                            <th>Assessment Definition</th>
                            <th>Entity Count</th>
                            </thead>
                            <tbody>
                            {#each groupedAssessments as assessmentInfo}
                                <tr on:click={() => selectDefinition(assessmentInfo)}
                                    class="clickable">
                                    <td>
                                        <Icon name={entity[assessmentInfo.assessmentDefinition.entityKind].icon}/>
                                    </td>
                                    <td>{assessmentInfo.assessmentDefinition.name}</td>
                                    <td>{_.sumBy(assessmentInfo.ratingEntityLists, d => _.size(d.entityReferences))}</td>
                                </tr>
                            {/each}
                            </tbody>
                        </table>
                    </div>
                </div>
            {:else}
                <NoData>There are no assessments for the current entity list and selected kind</NoData>
            {/if}
        </div>
        <br>
        <div class="col-sm-12">
            <div style="border-top: 1px dotted #eee; padding-top: 0.2em; margin-top: 0.2em">
                <button class="btn btn-skinny"
                        on:click={changeView}>
                    View default filters
                </button>
            </div>
        </div>
    {:else if activeMode === Modes.RATING}
        <div class="col-sm-12">
            <div>
                Filter by assessment rating for {selectedDefinition.assessmentDefinition.name}:
            </div>
            <div>
                <table class="table table-condensed table-hover small">
                    <thead>
                    <th>Rating</th>
                    <th>Entity Count</th>
                    </thead>
                    <tbody>
                    {#each selectedDefinition.ratingEntityLists as rating}
                        <tr on:click={() => selectRating(rating)}
                            class="clickable" class:selected={selectedRating === rating}>
                            <td>{rating.rating}</td>
                            <td>{_.size(rating.entityReferences)}</td>
                        </tr>
                    {/each}
                    </tbody>
                </table>
            </div>
        </div>
        <br>
        <div class="col-sm-12">
            <div  style="border-top: 1px dotted #eee; padding-top: 0.2em; margin-top: 0.2em">
                <button class="btn btn-skinny"
                        on:click={back}>
                    Back
                </button>
                |
                <button class="btn btn-skinny"
                        on:click={changeView}>
                    View default filters
                </button>
            </div>
        </div>
    {/if}
</div>



<style>

    .selected {
        background-color: #fffdcd;
    }

</style>