<script>
    import {mkSelectionOptions} from "../../../common/selector-utils";
    import AssessmentWidgetParameters from "./widgets/AssessmentWidgetParameters.svelte";
    import AppCostWidgetParameters from "./widgets/AppCostWidgetParameters.svelte";
    import AppCountWidgetParameters from "./widgets/AppCountWidgetParameters.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";


    export let primaryEntityRef;

    const widgets = [
      {
            parameterWidget: AppCostWidgetParameters,
            description: "Shows current cost and future cost info",
            label: "App Costs"
        }, {
            parameterWidget: AppCountWidgetParameters,
            description: "Shows current app count and future app count info",
            label: "App Counts"
        }, {
            label: "Assessments",
            description: "Allows user to select an assessment to overlay on the diagram",
            parameterWidget: AssessmentWidgetParameters
        }
    ];

    let focusWidget = null;
    let opts = null;

    $: opts = mkSelectionOptions(primaryEntityRef);


</script>

{#if focusWidget}
    <div class="help-block">{focusWidget.description}</div>

    <svelte:component this={focusWidget.parameterWidget}
                      {opts}/>
    <hr>
    <button class="btn btn-skinny"
            on:click={() => focusWidget = null}>
        Cancel
    </button>

{:else}
    {#each widgets as widget}
        <div>
            <button class="btn btn-skinny" on:click={() => focusWidget = widget}>
                <Icon name="plus"/>
                {widget.label}
            </button>
        </div>
    {/each}
{/if}
