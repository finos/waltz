<script>

    import {createEventDispatcher} from "svelte";
    import {entity} from "../../../common/services/enums/entity";
    import _ from "lodash";
    import NoData from "../../../common/svelte/NoData.svelte";

    export let diagrams = [];

    const dispatch = createEventDispatcher();

    function selectDiagram(diagram) {
        dispatch("select", diagram);
    }

</script>


<table class="table table-condensed table-hover">
    <thead>
    <tr>
        <th>Name</th>
        <th>Description</th>
        <th>Aggregated Entity Kind</th>
    </tr>
    </thead>
    <tbody>
    {#each diagrams as diagram}
        <tr on:click={() => selectDiagram(diagram)}
            class="clickable">
            <td>{diagram.name}</td>
            <td>{diagram.description || '-'}</td>
            <td>{_.get(entity, [diagram.aggregatedEntityKind, "name"], "Unknown")}</td>
        </tr>
    {:else}
        <tr>
            <td colspan="3">
                <NoData type="info">There are no entity overlay diagrams</NoData>
            </td>
        </tr>
    {/each}
    </tbody>
</table>
