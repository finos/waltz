<script context="module">
    import {writable} from "svelte/store";

    const selectedDefinition = writable(null);
    const useTargetDate = writable(false);
    const sliderVal = writable(0);
    const mode = writable("BAR");

    export function resetParameters() {
        mode.set("BAR");
        selectedDefinition.set(null);
        useTargetDate.set(false);
        sliderVal.set(0);
    }

</script>

<script>
    import AssessmentDefinitionPicker
        from "../../../../../common/svelte/entity-pickers/AssessmentDefinitionPicker.svelte";
    import {getContext} from "svelte";
    import {assessmentRenderMode, Modes} from "./store";
    import moment from "moment";
    import {timeFormat} from "d3-time-format";
    import DescriptionFade from "../../../../../common/svelte/DescriptionFade.svelte";

    const selectedOverlay = getContext("selectedOverlay");
    const selectedDiagram = getContext("selectedDiagram");
    const widgetParameters = getContext("widgetParameters");

    const fmt = timeFormat("%Y-%m-%d");

    function load(assessmentDef, targetDate) {
        if (assessmentDef) {
            $widgetParameters = {
                assessmentDefinitionId: assessmentDef.id,
                targetDate
            };

            $selectedOverlay = null;
        }
    }


    $: targetDate = $useTargetDate
        ? moment().set("date", 1).add($sliderVal, "months")
        : null;

    $: $assessmentRenderMode = Modes[$mode];

    $: {
        load($selectedDefinition, targetDate);
    }
</script>

{#if $selectedDefinition}
    <h5>
        {$selectedDefinition.name}
        <button class="btn btn-skinny"
                on:click={() => $selectedDefinition = null}>
            (Change assessment)
        </button>
    </h5>

    <div class="help-block">
        <DescriptionFade text={$selectedDefinition.description}/>
    </div>

{:else}
    <AssessmentDefinitionPicker onSelect={ad => $selectedDefinition = ad}
                                selectionFilter={ad => ad.entityKind === $selectedDiagram.aggregatedEntityKind}/>
{/if}


{#if $selectedDefinition}
    <br>

    <h4>Rendering Mode</h4>
    <p class="help-block">
        Controls how the assessment data will be displayed.
    </p>
    <label>
        <input style="display: inline-block;"
               type="radio"
               bind:group={$mode}
               name="assessmentRenderMode"
               value={"BAR"}>
        Bar Chart
    </label>

    <label>
        <input style="display: inline-block;"
               type="radio"
               bind:group={$mode}
               name="assessmentRenderMode"
               value={"BOX"}>
        Box Chart
    </label>

    <br>

    <h4>Date</h4>
    <p class="help-block">
        Calculate a future projected view of this assessment.
        This is based upon app retirements and app/function decomms (and replacements).
    </p>
    <label>
        <input type="checkbox"
               bind:checked={$useTargetDate}>
        Use target date?
    </label>


    <input id="future-date"
           type="range"
           min="0"
           max="120"
           disabled={!$useTargetDate}
           bind:value={$sliderVal}>
    <span>
        {targetDate
            ? fmt(targetDate)
            : '-'}
    </span>
{/if}

<style>
    label {
        font-weight: normal;
    }
</style>