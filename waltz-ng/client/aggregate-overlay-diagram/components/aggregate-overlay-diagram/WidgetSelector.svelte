<script>
    import {mkSelectionOptions} from "../../../common/selector-utils";
    import AssessmentWidgetParameters from "./widgets/assessments/AssessmentWidgetParameters.svelte";
    import TargetAppCostWidgetParameters from "./widgets/target-costs/TargetAppCostWidgetParameters.svelte";
    import AppCountWidgetParameters from "./widgets/app-counts/AppCountWidgetParameters.svelte";
    import AppCostWidgetParameters from "./widgets/app-costs/AppCostWidgetParameters.svelte";
    import BackingEntitiesWidgetParameters from "./widgets/backing-entities/BackingEntitiesWidgetParameters.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {getContext} from "svelte";
    import {entity} from "../../../common/services/enums/entity";
    import _ from "lodash";
    import AggregatedEntitiesWidgetParameters
        from "./widgets/aggregated-entities/AggregatedEntitiesWidgetParameters.svelte";

    export let primaryEntityRef;

    let displayedWidgets;

    const selectedOverlay = getContext("selectedOverlay");
    const selectedDiagram = getContext("selectedDiagram");
    const focusWidget = getContext("focusWidget");
    const filterParameters = getContext("filterParameters");
    const widgetParameters = getContext("widgetParameters");
    const selectionOptions = getContext("selectionOptions");

    const widgets = [
        {
            parameterWidget: TargetAppCostWidgetParameters,
            description: "Shows current cost and future cost info",
            label: "Target App Costs",
            aggregatedEntityKinds: [entity.APPLICATION.key]
        }, {
            parameterWidget: AppCostWidgetParameters,
            description: "Shows current app costs accounting for allocation percentages",
            label: "App Costs",
            aggregatedEntityKinds: [entity.APPLICATION.key]
        }, {
            parameterWidget: AppCountWidgetParameters,
            description: "Shows current app count and future app count info",
            label: "App Counts",
            aggregatedEntityKinds: [entity.APPLICATION.key]
        }, {
            label: "Assessments",
            description: "Allows user to select an assessment to overlay on the diagram",
            parameterWidget: AssessmentWidgetParameters,
            aggregatedEntityKinds: [entity.APPLICATION.key, entity.CHANGE_INITIATIVE.key]
        }, {
            label: "Backing Entities",
            description: "Displays the underlying entities which drive the overlays on the diagram",
            parameterWidget: BackingEntitiesWidgetParameters,
            aggregatedEntityKinds: [entity.APPLICATION.key, entity.CHANGE_INITIATIVE.key]
        }, {
            label: "Aggregated Entities",
            description: "Displays entities which are aggregated to populate the other overlay data",
            parameterWidget: AggregatedEntitiesWidgetParameters,
            aggregatedEntityKinds: [entity.APPLICATION.key, entity.CHANGE_INITIATIVE.key]
        }
    ];

    $: displayedWidgets = _.filter(
        widgets,
        d => _.includes(d.aggregatedEntityKinds, $selectedDiagram.aggregatedEntityKind));

    function onCancel() {
        $focusWidget = null;
        $selectedOverlay = null;
    }

    let opts = null;

    $: $selectionOptions = mkSelectionOptions(primaryEntityRef);

    function selectWidget(widget) {
        $widgetParameters = null;
        $focusWidget = widget;
    }

</script>


{#if $focusWidget}
    <div class="help-block">{$focusWidget.description}</div>

    <svelte:component this={$focusWidget.parameterWidget}/>

    <hr>
    <button class="btn btn-skinny"
            on:click={onCancel}>
        Cancel
    </button>

{:else}
    {#each displayedWidgets as widget}
        <div>
            <button class="btn btn-skinny"
                    on:click={() => selectWidget(widget)}>
                <Icon name="plus"/>
                {widget.label}
            </button>
        </div>
    {/each}
{/if}


