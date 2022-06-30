<script>

    import AssessmentRatingPicker from "../../../common/svelte/AssessmentRatingPicker.svelte";
    import {createEventDispatcher, getContext} from "svelte";
    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";
    import toasts from "../../../svelte-stores/toast-store";

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

    function definitionFilter(d) {
        return d.entityKind === $selectedDiagram.aggregatedEntityKind;
    }

    function close() {
        dispatch("close");
        // remove any 'empty' filters
        $filterParameters = _.reject(
            $filterParameters,
            d => _.isEmpty(d.ratingSchemeItems));
    }

    $: ratings = _
        .chain($filterParameters?.ratingSchemeItems)
        .map(d => d.name)
        .join(', ')
        .value();

</script>


<AssessmentRatingPicker on:select={selectRatings}
                        definitionFilter={definitionFilter}
                        selectedDefinition={$selectedFilter?.assessmentDefinition}
                        selectedRatings={$selectedFilter?.ratingSchemeItems}/>
<button class="btn btn-skinny"
        on:click={() => close()}>
    <Icon name="close"/>
    Close
</button>
