<script>
    import AssessmentDefinitionPicker
        from "./entity-pickers/AssessmentDefinitionPicker.svelte";
    import {ratingSchemeStore} from "../../svelte-stores/rating-schemes";
    import RatingPicker from "./RatingPicker.svelte";
    import {createEventDispatcher} from "svelte";
    import Markdown from "./Markdown.svelte";

    export let definitionFilter = () => true;
    export let selectedDefinition = null;
    export let selectedRatings = [];
    const dispatch = createEventDispatcher();
    const loadSchemesCall = ratingSchemeStore.loadAll(true);

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
    <h5>{selectedDefinition.name}
        <button class="btn btn-skinny"
                on:click={() => selectedDefinition = null}>
            (Change assessment)
        </button>
    </h5>
    <div class="help-block">
        <Markdown text={selectedDefinition.description}/>
    </div>

    <RatingPicker on:select={onRatingsSelect}
                  scheme={schemesById[selectedDefinition.ratingSchemeId]}
                  selectedRatings={selectedRatings}/>
{/if}
