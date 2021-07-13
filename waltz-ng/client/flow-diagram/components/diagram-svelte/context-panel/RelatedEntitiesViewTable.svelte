<script>
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {flowDiagramEntityStore} from "../../../../svelte-stores/flow-diagram-entity-store";
    import _ from "lodash";
    import model from "../store/model";
    import {changeInitiative} from "../../../../common/services/enums/change-initiative";
    import {createEventDispatcher} from "svelte";
    import RelatedEntityAddNodesPanel from "./RelatedEntityAddNodesPanel.svelte";
    import RelatedEntityAddFlowsPanel from "./RelatedEntityAddFlowsPanel.svelte";

    export let diagramId;
    export let canEdit;
    export let measurables;
    export let changeInitiatives;
    export let datatypes;

    let selectedKind;

    let dispatch = createEventDispatcher();

    const Modes = {
        OVERVIEW: "OVERVIEW",
        RELATED_ENTITY_ADD_NODES: "RELATED_ENTITY_ADD_NODES",
        RELATED_ENTITY_ADD_FLOWS: "RELATED_ENTITY_ADD_FLOWS",
    }

    let activeMode = Modes.OVERVIEW
    let selectedEntity = null;

    function removeEntity(evt) {
        const entity = evt.detail
        flowDiagramEntityStore.removeRelationship(diagramId, entity.data);
        model.removeRelationship(entity);
    }

    function addEntity(kind) {
        dispatch("select", kind);
    }

    function cancel() {
        selectedEntity = null;
        activeMode = Modes.OVERVIEW
    }

    function selectEntity(entity) {
        selectedEntity = entity
        if (entity.data.kind === 'DATA_TYPE') {
            activeMode = Modes.RELATED_ENTITY_ADD_FLOWS;
        } else if (entity.data.kind === 'CHANGE_INITIATIVE' || entity.data.kind === 'MEASURABLE') {
            activeMode = Modes.RELATED_ENTITY_ADD_NODES;
        } else {
            console.log("Cannot identify Entity Kind: " + entity.data.kind)
        }
    }

    function selectKind(kind) {
        if(selectedKind === kind){
            selectedKind = null;
        } else {
            selectedKind = kind;
        }
    }

</script>

{#if activeMode === Modes.OVERVIEW}

<div class="help-block">
    Related entities can be used to link this diagram to other pages in waltz.
    They can also be used to quickly generate diagrams or populate overlay groups.
</div>

<table class="table table-condensed">
    <thead><th></th></thead>
    <tbody>
        <tr class="clickable" on:click={() => selectKind("MEASURABLE")}>
            <td>
                <Icon name={selectedKind === 'MEASURABLE' ? "caret-down" : "caret-right"}/>
                Viewpoints - ({_.size(measurables)})
                {#if canEdit}
                    <button class="btn btn-skinny"
                            on:click={() => addEntity("MEASURABLE")}>
                        <Icon name="plus"/>Add
                    </button>
                {/if}
            </td>
        </tr>
        {#if selectedKind === 'MEASURABLE'}
            <tr>
                <div class:waltz-scroll-region-250={_.size(measurables) > 8}>
                    <table class="table table-condensed small table-hover entity-inner-table">
                        <tbody>
                        {#if _.isEmpty(measurables)}
                            <tr>
                                <td>No associated viewpoints</td>
                            </tr>
                        {:else}
                            {#each measurables as measurable}
                                <tr on:click={() => selectEntity(measurable)}
                                    class="clickable">
                                    <td>
                                        <Icon name="puzzle-piece"/>{measurable.data.name}
                                        <span class="small text-muted">
                                        {_.get(measurable.category, "name", "unknown")}
                                    </span>
                                    </td>
                                </tr>
                            {/each}
                        {/if}
                        </tbody>
                    </table>
                </div>
            </tr>
        {/if}
        <tr class="clickable" on:click={() => selectKind('CHANGE_INITIATIVE')}>
            <td>
                <Icon name={selectedKind === 'CHANGE_INITIATIVE' ? "caret-down" : "caret-right"}/>
                Change Initiatives - ({_.size(changeInitiatives)})
                {#if canEdit}
                    <button class="btn btn-skinny"
                            on:click={() => addEntity("CHANGE_INITIATIVE")}>
                        <Icon name="plus"/>Add
                    </button>
                {/if}
            </td>
        </tr>
        {#if selectedKind === 'CHANGE_INITIATIVE'}
            <tr>
                <div class:waltz-scroll-region-250={_.size(changeInitiatives) > 8}>
                    <table class="table table-condensed small table-hover entity-inner-table">
                        <tbody>
                            {#if _.isEmpty(changeInitiatives)}
                                <tr>
                                    <td>No associated change initiatives</td>
                                </tr>
                            {:else}
                            {#each changeInitiatives as changeInitiative}
                                <tr on:click={() => selectEntity(changeInitiative)}
                                    class="clickable">
                                    <td>
                                        <Icon name="paper-plane-o"/>{changeInitiative.data.name}
                                    </td>
                                </tr>
                            {/each}
                            {/if}
                        </tbody>
                    </table>
                </div>
            </tr>
        {/if}
        <tr class="clickable" on:click={() => selectKind('DATA_TYPE')}>
            <td>
                <Icon name={selectedKind === 'DATA_TYPE' ? "caret-down" : "caret-right"}/>
                Data Types - ({_.size(datatypes)})
                {#if canEdit}
                    <button class="btn btn-skinny"
                            on:click={() => addEntity("DATA_TYPE")}>
                        <Icon name="plus"/>Add
                    </button>
                {/if}
            </td>
        </tr>
        {#if selectedKind === 'DATA_TYPE'}
            <tr>
                <div class:waltz-scroll-region-250={_.size(datatypes) > 8}>
                    <table class="table table-condensed small table-hover entity-inner-table">
                        <tbody>
                            {#if _.isEmpty(datatypes)}
                                <tr>
                                    <td>No associated datatypes</td>
                                </tr>
                            {:else}
                            {#each datatypes as datatype}
                                <tr on:click={() => selectEntity(datatype)}
                                    class="clickable">
                                    <td>
                                        <Icon name="qrcode"/>{datatype.data.name}
                                    </td>
                                </tr>
                            {/each}
                            {/if}
                        </tbody>
                    </table>
                </div>
            </tr>
        {/if}
    </tbody>
</table>
{:else if activeMode === Modes.RELATED_ENTITY_ADD_NODES}
    <RelatedEntityAddNodesPanel entity={selectedEntity}
                                {canEdit}
                                on:cancel={() => activeMode = Modes.OVERVIEW}
                                on:remove={removeEntity}/>
{:else if activeMode === Modes.RELATED_ENTITY_ADD_FLOWS}
    <RelatedEntityAddFlowsPanel entity={selectedEntity}
                                {canEdit}
                                on:cancel={() => activeMode = Modes.OVERVIEW}
                                on:remove={removeEntity}/>
{/if}


<style>
    .entity-inner-table{
        margin-bottom: 0;
        border-bottom: solid #ccc 1px;
        border-top: none;
        margin-top: 0;
        padding-top: 0;
    }
</style>

