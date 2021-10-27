<script>

    import {assessmentRatingViewStore} from "../../../svelte-stores/assessment-rating-view-service";
    import _ from "lodash";

    export let primaryEntityRef;

    $: assessmentsCall = assessmentRatingViewStore.findFavouritesForEntity(primaryEntityRef, true);
    $: assessments = $assessmentsCall.data;

</script>

{#if !_.isEmpty(assessments)}
    <table class="table table-condensed small">
        <thead>
            <th width="50%">Favourite assessments:</th>
            <th width="50%"></th>
        </thead>
        <tbody>
        {#each assessments as assessment}
            <tr>
                <td>{assessment.assessmentDefinition.name}</td>
                <td>
                    <span class="indicator"
                          style={`background-color: ${assessment.ratingDefinition.color}`}>
                    </span>
                    {assessment.ratingDefinition.name}
                </td>
            </tr>
        {/each}
        </tbody>
    </table>
{/if}


<style>
    .indicator {
        display: inline-block;
        height: 0.9em;
        width: 1em;
        border: 1px solid #ccc;
        border-radius: 2px;
    }
</style>