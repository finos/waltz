<script>

    import {findAssociatedApps, selectApplication} from "../process-diagram-utils";
    import {appsByDiagramMeasurableId, selectedObject, selectedApp, highlightedActivities} from "../diagram-store";
    import _ from "lodash";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";

    $: appsToDisplay = _
        .chain(findAssociatedApps($appsByDiagramMeasurableId, $selectedObject))
        .map(d => d.applicationRef)
        .sortBy(d => d.name)
        .value();

</script>

<h4><EntityLink ref={$selectedObject.waltzReference}/></h4>

<table class="table table-condensed small">
    <thead>
        <th>
            Associated Application
        </th>
        <th>
            Ext Id
        </th>
    </thead>
    <tbody>
        {#each appsToDisplay as app}
            <tr class="clickable"
                on:click={() => selectApplication(app)}>
                <td>{app.name}</td>
                <td>{app.externalId}</td>
            </tr>
            {:else}
            <tr>
                <td colspan="2">No applications are associated to this Activity</td>
            </tr>
        {/each}
    </tbody>
</table>