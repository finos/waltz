<script>
    import AssessmentDefinitionPicker from "../../report-grid/components/svelte/pickers/AssessmentDefinitionPicker.svelte";
    import {ratingSchemeStore} from "../../svelte-stores/rating-schemes";
    import RatingPicker from "./RatingPicker.svelte";
    import {createEventDispatcher} from "svelte";
    import Markdown from "./Markdown.svelte";

    export let definitionFilter = () => true;

    const dispatch = createEventDispatcher();
    const loadSchemesCall = ratingSchemeStore.loadAll(true);

    let selectedDefinition = null;
    let selectedRatings = null;
    let schemesById = {};

    function onDefinitionSelect(def) {
        selectedDefinition = def;
    }

    function onRatingsSelect(evt) {
        selectedRatings = evt.detail;
        const emittedEvent = {assessmentDefinition: selectedDefinition, ratingSchemeItems: selectedRatings};
        dispatch("select", emittedEvent);
    }

    $: schemesById = _.keyBy($loadSchemesCall.data, d => d.id);
</script>


{#if !selectedDefinition}
    <AssessmentDefinitionPicker selectionFilter={definitionFilter}
                                onSelect={onDefinitionSelect} />
{:else}
    <h4>{selectedDefinition.name}</h4>
    <div class="help-text">
        <Markdown text={selectedDefinition.description}/>
    </div>

    <RatingPicker on:select={onRatingsSelect}
                  scheme={schemesById[selectedDefinition.ratingSchemeId]}/>

    <button class="btn btn-link"
            on:click={() => selectedDefinition = null}>
        Cancel
    </button>
{/if}
