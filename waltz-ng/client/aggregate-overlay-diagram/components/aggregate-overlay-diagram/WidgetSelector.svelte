<script>
    import {mkSelectionOptions} from "../../../common/selector-utils";
    import AssessmentWidgetParameters from "./widgets/assessments/AssessmentWidgetParameters.svelte";
    import TargetAppCostWidgetParameters from "./widgets/target-costs/TargetAppCostWidgetParameters.svelte";
    import AppCountWidgetParameters from "./widgets/app-counts/AppCountWidgetParameters.svelte";
    import AppCostWidgetParameters from "./widgets/app-costs/AppCostWidgetParameters.svelte";
    import BackingEntitiesWidgetParameters from "./widgets/backing-entities/BackingEntitiesWidgetParameters.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";


    export let primaryEntityRef;

    const widgets = [
        {
            parameterWidget: TargetAppCostWidgetParameters,
            description: "Shows current cost and future cost info",
            label: "Target App Costs"
        }, {
            parameterWidget: AppCostWidgetParameters,
            description: "Shows current app costs accounting for allocation percentages",
            label: "App Costs"
        }, {
            parameterWidget: AppCountWidgetParameters,
            description: "Shows current app count and future app count info",
            label: "App Counts"
        }, {
            label: "Assessments",
            description: "Allows user to select an assessment to overlay on the diagram",
            parameterWidget: AssessmentWidgetParameters
        }, {
            label: "Backing Entities",
            description: "Displays the underlying entities which drive the overlays on the diagram",
            parameterWidget: BackingEntitiesWidgetParameters
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
