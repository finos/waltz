<script>

    import {findAssociatedApps, selectApplication} from "../process-diagram-utils";
    import {appsByDiagramMeasurableId, selectedObject} from "../diagram-store";
    import _ from "lodash";
    import EntityInfoPanel from "../../../../playpen/1/EntityInfoPanel.svelte";

    $: appsToDisplay = _
        .chain(findAssociatedApps($appsByDiagramMeasurableId, $selectedObject))
        .map(d => d.applicationRef)
        .value();

</script>

<EntityInfoPanel primaryEntityRef={$selectedObject.waltzReference}>

    <div slot="post-header">

        <div class={_.size(appsToDisplay) > 10 ? "waltz-scroll-region-250 scroll-activities" : ""}>
            <table class="table table-condensed table-hover small">
                <thead>
                <th width="50%">
                    Associated Application
                </th>
                <th width="50%">
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
        </div>

    </div>
</EntityInfoPanel>