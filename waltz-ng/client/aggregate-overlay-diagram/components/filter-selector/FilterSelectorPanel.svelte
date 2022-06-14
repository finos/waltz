<script>

    import AssessmentRatingPicker from "../../../common/svelte/AssessmentRatingPicker.svelte";
    import {getContext} from "svelte";
    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";

    const filterParameters = getContext("filterParameters");
    const selectedDiagram = getContext("selectedDiagram");

    function selectRatings(evt) {
        $filterParameters = evt.detail;
    }

    $: ratings = _
        .chain($filterParameters?.ratingSchemeItems)
        .map(d => d.name)
        .join(', ')
        .value();

    function definitionFilter(d) {
        return d.entityKind === $selectedDiagram.aggregatedEntityKind;
    }

</script>

<h4>
    <Icon name="filter"/>
    Filters
</h4>
<AssessmentRatingPicker on:select={selectRatings}
                        definitionFilter={definitionFilter}
                        selectedDefinition={$filterParameters?.assessmentDefinition}
                        selectedRatings={$filterParameters?.ratingSchemeItems}/>
{#if $filterParameters}
    <button class="btn btn-skinny"
            on:click={() => $filterParameters = null}>
        <Icon name="ban"/>
        Clear filters
    </button>
{/if}
