<script>

    import {findAssociatedApps} from "./process-diagram-utils";
    import {appsByDiagramMeasurableId, selectedObject} from "./diagram-store";
    import _ from "lodash";
    import ActivityContextPanel from "./ActivityContextPanel.svelte";
    import DecisionContextPanel from "./DecisionContextPanel.svelte";
    import DefaultContextPanel from "./DefaultContextPanel.svelte"
    import Activity from "./Activity.svelte";
    import Decision from "./Decision.svelte";

    $: appsToDisplay = _
        .chain(findAssociatedApps($appsByDiagramMeasurableId, $selectedObject))
        .map(d => d.applicationRef)
        .sortBy(d => d.name)
        .value();


    $: console.log({appsToDisplay});

    function determineContextPanel(selectedObject) {
        switch (selectedObject?.objectType) {
            case "Activity":
                return ActivityContextPanel;
            // case "Event":
            //     return Event;
            // case "NavigationCell":
            // case "Text":
            //     return TextCell;
            case "Decision":
                return DecisionContextPanel;
            // case "Boundary":
            //     return Boundary;
            default:
                return DefaultContextPanel;
        }
    }

    $: comp = determineContextPanel($selectedObject);



</script>

<svelte:component this={comp}/>