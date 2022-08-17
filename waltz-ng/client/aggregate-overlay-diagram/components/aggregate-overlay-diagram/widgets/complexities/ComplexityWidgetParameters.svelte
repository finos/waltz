<script context="module">
    import {writable} from "svelte/store";
    import {getContext} from "svelte";
    import _ from "lodash";
    import ComplexityKindPicker from "../../../../../report-grid/components/svelte/pickers/ComplexityKindPicker.svelte";

    const selectedComplexityKind = writable(null);

    export function resetParameters() {
        selectedComplexityKind.set(null);
    }

</script>

<script>

    const selectedOverlay = getContext("selectedOverlay");
    const widgetParameters = getContext("widgetParameters");

    const Modes = {
        COMPLEXITY_KIND_PICKER: "COMPLEXITY_KIND_PICKER",
        SUMMARY: "SUMMARY"
    }


    let activeMode = $selectedComplexityKind
        ? Modes.SUMMARY
        : Modes.COMPLEXITY_KIND_PICKER;

    function onSelect(costKind) {
        $selectedComplexityKind = costKind;

        $widgetParameters = {
            complexityKindId: $selectedComplexityKind.id,
        };

        $selectedOverlay = null;

        activeMode = Modes.SUMMARY;
    }

    function changeComplexityKind() {
        activeMode = Modes.COMPLEXITY_KIND_PICKER
        $selectedComplexityKind = null;
    }

    $: incompleteSelection = _.isEmpty($selectedComplexityKind);

</script>


{#if activeMode === Modes.COMPLEXITY_KIND_PICKER}
    <div class="help-block">
        Select a complexity kind from the list below.
    </div>
    <ComplexityKindPicker onSelect={onSelect}/>
{:else if activeMode === Modes.SUMMARY}
    <div>
        Selected Complexity Kind: {$selectedComplexityKind.name}
    </div>
    <button class="btn btn-skinny"
            on:click={changeComplexityKind}>
        Change complexity kind
    </button>
{/if}
