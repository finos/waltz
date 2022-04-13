<script>
    import AppCountWidget from "./widgets/AppCountWidget.svelte";
    import AppCostWidget from "./widgets/AppCostWidget.svelte";
    import DebugWidget from "./widgets/DebugWidget.svelte";
    import ExampleWidget1 from "./widgets/ExampleWidget1.svelte";
    import {createEventDispatcher} from "svelte";
    import {overlayDiagramStore} from "../../../svelte-stores/overlay-diagram-store";
    import {mkSelectionOptions} from "../../../common/selector-utils";

    const dispatcher = createEventDispatcher();

    // const ref = {id: 11785, kind: "APP_GROUP"};
    const ref = {id: 95, kind: "ORG_UNIT"};
    const opts  = mkSelectionOptions(ref);

    const lookupMap = {
        DEBUG: { widget: DebugWidget, dataProvider: () => {} },
        APP_COSTS: { widget: AppCostWidget, dataProvider: () => overlayDiagramStore.findAppCostForDiagram(1, opts) },
        APP_COUNTS: { widget: AppCountWidget, dataProvider: () => overlayDiagramStore.findAppCountsForDiagram(1, opts) },
        EXAMPLE: { widget: ExampleWidget1, dataProvider: () => overlayDiagramStore.findAppCountsForDiagram(1, opts) },
    }

    function select(widgetKey) {
        const stuff = lookupMap[widgetKey];
        dispatcher("change", { widget: stuff.widget, dataProvider: stuff.dataProvider()});
    }
</script>


<button on:click={() => select("DEBUG")}>Switch To Debug</button>
<button on:click={() => select("APP_COUNTS")}>Switch To Stats</button>
<button on:click={() => select("APP_COSTS")}>Switch To Costs</button>
<button on:click={() => select("EXAMPLE")}>Switch To Example1</button>
