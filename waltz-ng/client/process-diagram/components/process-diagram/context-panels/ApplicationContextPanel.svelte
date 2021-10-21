<script>

    import {selectDiagramObject} from "../process-diagram-utils";
    import {highlightedActivities, selectedApp} from "../diagram-store";
    import EntityInfoPanel from "../../../../playpen/1/EntityInfoPanel.svelte";
</script>

<EntityInfoPanel primaryEntityRef={$selectedApp}>
    <div slot="post-header">

        <div class={_.size($highlightedActivities) > 10 ? "waltz-scroll-region-250 scroll-activities" : ""}>
            <table class="table table-condensed table-hover small">
                <thead>
                <th width="100%">
                    Associated Activity
                </th>
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
        </div>

    </div>
</EntityInfoPanel>

<style>
    .scroll-activities {
        margin-top: 1em;
        margin-bottom: 1em;
    }
</style>