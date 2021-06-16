<script>
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {flowDiagramEntityStore} from "../../../../svelte-stores/flow-diagram-entity-store";
    import _ from "lodash";
    import model from "../store/model";
    import {changeInitiative} from "../../../../common/services/enums/change-initiative";
    import {createEventDispatcher} from "svelte";

    export let diagramId;
    export let measurables;
    export let changeInitiatives;

    let dispatch = createEventDispatcher();

    function removeEntity(entity) {
        flowDiagramEntityStore.removeRelationship(diagramId, entity.data);
        model.removeRelationship(entity);
    }

    function addEntity(kind){
        dispatch("select", kind);
    }

</script>

<strong>Measurables:</strong>
<table class="table table-condensed small">
    <!-- IF NONE -->
    {#if _.isEmpty(measurables)}
    <tr>
        <td colspan="2">No associated viewpoints</td>
    </tr>
    {:else}
    <!-- LIST -->
    <tbody>
    {#each measurables as measurable}
    <tr>
        <td>
            <div >{measurable.data.name}</div>
            <div class="small text-muted">
                {_.get(measurable.category, "name", "unknown")}
            </div>
        </td>
        <td>
            <button on:click={() => removeEntity(measurable)}
               class="clickable">
                <Icon name="trash"/>Remove
            </button>
        </td>
    </tr>
    {/each}
    </tbody>
    {/if}

    <!-- FOOTER-->
    <tfoot>
    <tr>
        <td colspan="2">
            <button class="btn btn-skinny"
                    on:click={() => addEntity("MEASURABLE")}>
                <Icon name="plus"/>Add
            </button>
        </td>
    </tr>
    </tfoot>
</table>

<strong>Change Initiatives:</strong>
<table class="table table-condensed small">
    <!-- IF NONE -->
    {#if _.isEmpty(changeInitiatives)}
        <tr>
            <td colspan="2">No associated change initiatives</td>
        </tr>
    {:else}
        <!-- LIST -->
        <tbody>
        {#each changeInitiatives as changeInitiative}
            <tr>
                <td>
                    <div>{changeInitiative.data.name}</div>
                </td>
                <td>
                    <button on:click={() => removeEntity(changeInitiative)}
                            class="clickable">
                        <Icon name="trash"/>Remove
                    </button>
                </td>
            </tr>
        {/each}
        </tbody>
    {/if}

    <!-- FOOTER-->
    <tfoot>
    <tr>
        <td colspan="2">
            <button class="btn btn-skinny"
                    on:click={() => addEntity("CHANGE_INITIATIVE")}>
                <Icon name="plus"/>Add
            </button>
        </td>
    </tr>
    </tfoot>
</table>