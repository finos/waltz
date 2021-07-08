<script>
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {flowDiagramEntityStore} from "../../../../svelte-stores/flow-diagram-entity-store";
    import _ from "lodash";
    import model from "../store/model";
    import {changeInitiative} from "../../../../common/services/enums/change-initiative";
    import {createEventDispatcher} from "svelte";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";

    export let diagramId;
    export let canEdit;
    export let measurables;
    export let changeInitiatives;
    export let datatypes;

    let dispatch = createEventDispatcher();

    function removeEntity(entity) {
        flowDiagramEntityStore.removeRelationship(diagramId, entity.data);
        model.removeRelationship(entity);
    }

    function addEntity(kind) {
        dispatch("select", kind);
    }

</script>

<div style="padding-bottom: 1em">
    <strong>Measurables:</strong>
    <div class:waltz-scroll-region-250={_.size(measurables) > 4}>
        <table class="table table-condensed small"
               style="margin-bottom: 0; border-bottom: solid #ccc 1px" >
            <!-- IF NONE -->
            {#if _.isEmpty(measurables)}
                <tr>
                    <td colspan="2">No associated viewpoints</td>
                </tr>
            {:else}
                <thead>
                <th width="80%"/>
                <th width="20%"/>
                </thead>
                <!-- LIST -->
                <tbody>
                {#each measurables as measurable}
                    <tr>
                        <td>
                            <div><EntityLink ref={measurable.data}/></div>
                            <div class="small text-muted">
                                {_.get(measurable.category, "name", "unknown")}
                            </div>
                        </td>
                        <td>
                            {#if canEdit}
                                <button on:click={() => removeEntity(measurable)}
                                        class="clickable">
                                    <Icon name="trash"/>Remove
                                </button>
                            {/if}
                        </td>
                    </tr>
                {/each}
                </tbody>
            {/if}
        </table>
    </div>
    {#if canEdit}
        <button class="btn btn-skinny"
                on:click={() => addEntity("MEASURABLE")}>
            <Icon name="plus"/>Add
        </button>
    {/if}
</div>

<div style="padding-bottom: 1em;">
    <strong>Change Initiatives:</strong>
    <table class="table table-condensed small"
           style="margin-bottom: 0; border-bottom: solid #ccc 1px">
        <!-- IF NONE -->
        {#if _.isEmpty(changeInitiatives)}
            <tr>
                <td colspan="2">No associated change initiatives</td>
            </tr>
        {:else}
            <thead>
            <th width="80%"/>
            <th width="20%"/>
            </thead>
            <!-- LIST -->
            <tbody>
            {#each changeInitiatives as changeInitiative}
                <tr>
                    <td>
                        <div><EntityLink ref={changeInitiative.data}/></div>
                    </td>
                    <td>
                        {#if canEdit}
                            <button on:click={() => removeEntity(changeInitiative)}
                                    class="clickable">
                                <Icon name="trash"/>Remove
                            </button>
                        {/if}
                    </td>
                </tr>
            {/each}
            </tbody>
        {/if}
    </table>
    {#if canEdit}
        <button class="btn btn-skinny"
                on:click={() => addEntity("CHANGE_INITIATIVE")}>
            <Icon name="plus"/>Add
        </button>
    {/if}
</div>


<div style="padding-bottom: 1em">
    <strong>Datatypes:</strong>
    <div class:waltz-scroll-region-250={_.size(datatypes) > 4}>
        <table class="table table-condensed small"
               style="margin-bottom: 0; border-bottom: solid #ccc 1px" >
            <!-- IF NONE -->
            {#if _.isEmpty(datatypes)}
                <tr>
                    <td colspan="2">No associated datatypes</td>
                </tr>
            {:else}
                <thead>
                <th width="80%"/>
                <th width="20%"/>
                </thead>
                <!-- LIST -->
                <tbody>
                {#each datatypes as datatype}
                    <tr>
                        <td>
                            <div><EntityLink ref={datatype.data}/></div>
                        </td>
                        <td>
                            {#if canEdit}
                                <button on:click={() => removeEntity(datatype)}
                                        class="clickable">
                                    <Icon name="trash"/>Remove
                                </button>
                            {/if}
                        </td>
                    </tr>
                {/each}
                </tbody>
            {/if}
        </table>
    </div>
    {#if canEdit}
        <button class="btn btn-skinny"
                on:click={() => addEntity("DATA_TYPE")}>
            <Icon name="plus"/>Add
        </button>
    {/if}
</div>

