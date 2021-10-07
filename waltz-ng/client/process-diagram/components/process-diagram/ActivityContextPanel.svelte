<script>

    import {findAssociatedApps} from "./process-diagram-utils";
    import {appsByDiagramMeasurableId, selectedObject} from "./diagram-store";
    import _ from "lodash";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";

    $: appsToDisplay = _
        .chain(findAssociatedApps($appsByDiagramMeasurableId, $selectedObject))
        .map(d => d.applicationRef)
        .sortBy(d => d.name)
        .value();

</script>

<h4><EntityLink ref={$selectedObject.waltzReference}/></h4>

<table class="table table-condensed">
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
            <tr>
                <td><EntityLink ref={app}/></td>
                <td>{app.externalId}</td>
            </tr>
            {:else}
            <tr>
                <td colspan="2">No applications are associated to this Activity</td>
            </tr>
        {/each}
    </tbody>
</table>