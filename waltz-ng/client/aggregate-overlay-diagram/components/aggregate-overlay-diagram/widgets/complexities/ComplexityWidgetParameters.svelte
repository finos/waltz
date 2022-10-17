<script context="module">
    import {writable} from "svelte/store";
    import {getContext} from "svelte";
    import ComplexityKindPicker from "../../../../../common/svelte/entity-pickers/ComplexityKindPicker.svelte";

    const selectedComplexityKinds = writable([]);
    const mode = writable("TOTAL");

    export function resetParameters() {
        selectedComplexityKinds.set([]);
    }

</script>

<script>

    import _ from "lodash";
    import {ComplexityModes, complexityRenderMode} from "./store";

    const selectedOverlay = getContext("selectedOverlay");
    const widgetParameters = getContext("widgetParameters");

    const Modes = {
        COMPLEXITY_KIND_PICKER: "COMPLEXITY_KIND_PICKER",
        SUMMARY: "SUMMARY"
    }

    $: $complexityRenderMode = ComplexityModes[$mode];

    let activeMode = !_.isEmpty($selectedComplexityKinds)
        ? Modes.SUMMARY
        : Modes.COMPLEXITY_KIND_PICKER;

    function onSelect() {
        $widgetParameters = {
            complexityKindIds: complexityKindIds,
        };

        $selectedOverlay = null;

        activeMode = Modes.SUMMARY;
    }

    function onSelectComplexityKind(complexityKind) {
        $selectedComplexityKinds = _.concat($selectedComplexityKinds, complexityKind)
    }

    function changeComplexityKind() {
        activeMode = Modes.COMPLEXITY_KIND_PICKER
        $selectedComplexityKinds = [];
    }

    $: incompleteSelection = _.isEmpty($selectedComplexityKinds);

    $: complexityKindIds = _.map($selectedComplexityKinds, d => d.id);


</script>


{#if activeMode === Modes.COMPLEXITY_KIND_PICKER}
    <div class="help-block">
        Select a complexity kind from the list below.
    </div>
    <ComplexityKindPicker onSelect={onSelectComplexityKind}
                          selectionFilter={ck => !_.includes(complexityKindIds, ck.id)}/>
    {#if !_.isEmpty($selectedComplexityKinds)}
        <ul>
            {#each $selectedComplexityKinds as kind}
                <li>
                    {kind.name}
                </li>
            {/each}
        </ul>
    {/if}
    <button class="btn btn-success"
            on:click={onSelect}
            disabled={incompleteSelection}>
        Load data
    </button>
{:else if activeMode === Modes.SUMMARY}
    <div>
        Selected Complexity Kinds:
        <ul>
            {#each $selectedComplexityKinds as kind}
                <li>
                    {kind.name}
                </li>
            {/each}
        </ul>
    </div>
    <h4>Rendering Mode</h4>
    <p class="help-block">
        Controls the complexity statistic displayed.
    </p>
    <label>
        <input style="display: inline-block;"
               type="radio"
               bind:group={$mode}
               name="complexityRenderMode"
               value={"TOTAL"}>
        Total
    </label>

    <label>
        <input style="display: inline-block;"
               type="radio"
               bind:group={$mode}
               name="complexityRenderMode"
               value={"AVERAGE"}>
        Average
    </label>
    <div>
        <button class="btn btn-skinny"
                on:click={changeComplexityKind}>
            Change complexity kind
        </button>
    </div>
{/if}
