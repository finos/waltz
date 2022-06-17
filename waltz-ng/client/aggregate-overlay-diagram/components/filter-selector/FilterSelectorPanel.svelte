<script>

    import AssessmentRatingPicker from "../../../common/svelte/AssessmentRatingPicker.svelte";
    import {createEventDispatcher, getContext} from "svelte";
    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";

    const filterParameters = getContext("filterParameters");
    const selectedFilter = getContext("selectedFilter");
    const selectedDiagram = getContext("selectedDiagram");

    const dispatch = createEventDispatcher();

    function selectRatings(evt) {

        const filter = evt.detail;

        const existingFilterForDefn = _.find(
            $filterParameters,
            d => d.assessmentDefinition.id === filter.assessmentDefinition.id)

        const withoutExisting = _.without($filterParameters, existingFilterForDefn);

        $filterParameters = _.concat(withoutExisting, [filter]);
    }

    $: ratings = _
        .chain($filterParameters?.ratingSchemeItems)
        .map(d => d.name)
        .join(', ')
        .value();

    function definitionFilter(d) {
        return d.entityKind === $selectedDiagram.aggregatedEntityKind;
    }

    function cancel() {
        dispatch("cancel");
    }

</script>


<AssessmentRatingPicker on:select={selectRatings}
                        definitionFilter={definitionFilter}
                        selectedDefinition={$selectedFilter?.assessmentDefinition}
                        selectedRatings={$selectedFilter?.ratingSchemeItems}/>
{#if !_.isEmpty($selectedFilter)}
    <button class="btn btn-skinny"
            on:click={() => $filterParameters = _.without($filterParameters, $selectedFilter)}>
        <Icon name="ban"/>
        Clear filter
    </button>
{/if}
{#if !_.isEmpty($filterParameters)}
    <button class="btn btn-skinny"
            on:click={() => cancel()}>
        Close
    </button>
{/if}
