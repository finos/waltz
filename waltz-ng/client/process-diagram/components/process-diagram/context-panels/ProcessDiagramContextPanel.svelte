<script>

    import {findAssociatedApps} from "../process-diagram-utils";
    import {appsByDiagramMeasurableId, selectedObject} from "../diagram-store";
    import _ from "lodash";
    import ActivityContextPanel from "./ActivityContextPanel.svelte";
    import DecisionContextPanel from "./DecisionContextPanel.svelte";
    import DefaultContextPanel from "./DefaultContextPanel.svelte"
    import NavigationCellContextPanel from "./NavigationCellContextPanel.svelte";
    import Activity from "../svg-elems/Activity.svelte";
    import Decision from "../svg-elems/Decision.svelte";

    $: appsToDisplay = _
        .chain(findAssociatedApps($appsByDiagramMeasurableId, $selectedObject))
        .map(d => d.applicationRef)
        .sortBy(d => d.name)
        .value();


    function determineContextPanel(selectedObject) {
        switch (selectedObject?.objectType) {
            case "Activity":
                return ActivityContextPanel;
            case "Text":
                return NavigationCellContextPanel;
            case "Decision":
                return DecisionContextPanel;
            default:
                return DefaultContextPanel;
        }
    }

    $: comp = determineContextPanel($selectedObject);



</script>

<svelte:component this={comp}/>