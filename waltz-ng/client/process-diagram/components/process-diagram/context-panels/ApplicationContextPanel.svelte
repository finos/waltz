<script>

    import {selectDiagramObject} from "../process-diagram-utils";
    import {highlightedActivities, selectedApp} from "../diagram-store";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte"
</script>

<h4><EntityLink ref={$selectedApp}/></h4>

<table class="table table-condensed table-hover small">
    <thead>
        <th>
            Associated Activity
        </th>
    </thead>
    <tbody>
        {#each $highlightedActivities as activity}
            <tr class="clickable"
                on:click={() => selectDiagramObject(activity)}>
                <td>{activity.name}</td>
            </tr>
            {:else}
            <tr>
                <td>No activities are associated to this application</td>
            </tr>
        {/each}
    </tbody>
</table>