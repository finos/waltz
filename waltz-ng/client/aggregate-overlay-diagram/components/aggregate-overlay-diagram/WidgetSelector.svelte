<script>
    import AppCountWidget from "./widgets/AppCountWidget.svelte";
    import AppCostWidget from "./widgets/AppCostWidget.svelte";
    import DebugWidget from "./widgets/DebugWidget.svelte";
    import ExampleWidget1 from "./widgets/ExampleWidget1.svelte";
    import {createEventDispatcher, getContext} from "svelte";
    import {aggregateOverlayDiagramStore} from "../../../svelte-stores/aggregate-overlay-diagram-store";
    import {mkSelectionOptions} from "../../../common/selector-utils";

    export let primaryEntityRef;

    const selectedDiagram = getContext("selectedDiagram");

    const dispatcher = createEventDispatcher();

    const opts = mkSelectionOptions(primaryEntityRef);

    const lookupMap = {
        DEBUG: {
            widget: DebugWidget, dataProvider: () => {
            }
        },
        APP_COSTS: {
            widget: AppCostWidget,
            dataProvider: () => aggregateOverlayDiagramStore.findAppCostForDiagram($selectedDiagram.id, opts)
        },
        APP_COUNTS: {
            widget: AppCountWidget,
            dataProvider: () => aggregateOverlayDiagramStore.findAppCountsForDiagram($selectedDiagram.id, opts)
        },
        EXAMPLE: {
            widget: ExampleWidget1,
            dataProvider: () => aggregateOverlayDiagramStore.findAppCountsForDiagram($selectedDiagram.id, opts)
        },
    }

    function select(widgetKey) {
        const stuff = lookupMap[widgetKey];
        dispatcher("change", {widget: stuff.widget, dataProvider: stuff.dataProvider()});
    }
</script>


<div>
    <button class="btn btn-default" style="margin-top: 1em" on:click={() => select("DEBUG")}>Switch To Debug</button>
</div>
<div>
    <button class="btn btn-default" style="margin-top: 0.5em" on:click={() => select("APP_COUNTS")}>Switch To Stats
    </button>
</div>
<div>
    <button class="btn btn-default" style="margin-top: 0.5em" on:click={() => select("APP_COSTS")}>Switch To Costs
    </button>
</div>
<div>
    <button class="btn btn-default" style="margin-top: 0.5em" on:click={() => select("EXAMPLE")}>Switch To Example1
    </button>
</div>
