<script>

    import AssessmentRatingPicker from "../../../common/svelte/AssessmentRatingPicker.svelte";
    import {getContext} from "svelte";
    import _ from "lodash";

    const Modes = {
        PICKER: "PICKER",
        VIEW: "VIEW"
    }

    let activeMode = Modes.VIEW

    const filterParameters = getContext("filterParameters");

    function selectRatings(evt) {
        $filterParameters = evt.detail;
    }

    $: ratings = _
        .chain($filterParameters?.ratingSchemeItems)
        .map(d => d.name)
        .join(', ')
        .value();

    function definitionFilter(d) {
        return d.entityKind === "APPLICATION";
    }

</script>


{#if activeMode === Modes.VIEW}
    {#if $filterParameters}
        <h4>
            Selected category: {$filterParameters?.assessmentDefinition.name}
        </h4>
        <h4>
            Selected ratings: {ratings}
        </h4>
        <button class="btn btn-skinny"
                on:click={() => $filterParameters = null}>
            Clear filters
        </button>
    {:else }
        <span>No filters have been selected.
            <button class="btn btn-skinny"
                    on:click={() => activeMode =Modes.PICKER}>
                Pick one now
            </button>
        </span>
    {/if}
{:else if activeMode = Modes.PICKER}
    <AssessmentRatingPicker on:select={selectRatings}
                            definitionFilter={definitionFilter}/>
    <br>
    <button class="btn btn-skinny"
            on:click={() => activeMode = Modes.VIEW}>
        View selection
    </button>
{/if}
