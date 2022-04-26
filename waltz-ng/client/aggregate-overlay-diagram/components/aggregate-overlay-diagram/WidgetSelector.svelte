<script>
    import DebugWidget from "./widgets/DebugWidget.svelte";
    import ExampleWidget1 from "./widgets/ExampleWidget1.svelte";
    import {createEventDispatcher, getContext} from "svelte";
    import {aggregateOverlayDiagramStore} from "../../../svelte-stores/aggregate-overlay-diagram-store";
    import {mkSelectionOptions} from "../../../common/selector-utils";
    import AssessmentWidgetParameters from "./widgets/AssessmentWidgetParameters.svelte";
    import AppCostWidgetParameters from "./widgets/AppCostWidgetParameters.svelte";
    import AppCountWidgetParameters from "./widgets/AppCountWidgetParameters.svelte";

    export let primaryEntityRef;

    const selectedDiagram = getContext("selectedDiagram");
    const overlayData = getContext("overlayData");
    const widget = getContext("widget");

    const dispatcher = createEventDispatcher();

    const widgets = [
        {
            widget: DebugWidget, dataProvider: () => {},
            description: "Shows basic debug information",
            label: "Debug"
        }, {
            parameterWidget: AppCostWidgetParameters,
            description: "Shows current cost and future cost info",
            label: "App Costs"
        }, {
            parameterWidget: AppCountWidgetParameters,
            description: "Shows current app count and future app count info",
            label: "App Counts"
        }, {
            widget: ExampleWidget1,
            dataProvider: (opts) => aggregateOverlayDiagramStore.findAppCountsForDiagram($selectedDiagram.id, opts),
            description: "Test widget",
            label: "Example 1"
        }, {
            label: "Assessments",
            description: "Allows user to select an assessment to overlay on the diagram",
            parameterWidget: AssessmentWidgetParameters
        }
    ];

    let focusWidget = null;
    let overlayDataCall = null;
    let opts = null;

    $: opts = mkSelectionOptions(primaryEntityRef);

    function invoke(w) {
        overlayDataCall = w.dataProvider(opts);
        $widget = w;
    }


    $: {
        $overlayData = $overlayDataCall?.data;
    }

</script>

{#if focusWidget}
    <div class="help-block">{focusWidget.description}</div>

    {#if (focusWidget.parameterWidget) }
        <svelte:component this={focusWidget.parameterWidget}
                          {opts}/>
    {:else}
        <button class="btn btn-default"
                style="margin-top: 1em"
                on:click={() => invoke(focusWidget)}>
            {focusWidget.label}
        </button>
    {/if}
    <hr>
    <button class="btn btn-skinny" on:click={() => focusWidget = null}>
        Cancel
    </button>
{:else}
    {#each widgets as widget}
        <div>
            <button class="btn btn-skinny" on:click={() => focusWidget = widget}>
                Configure {widget.label}
            </button>
        </div>
    {/each}
{/if}
