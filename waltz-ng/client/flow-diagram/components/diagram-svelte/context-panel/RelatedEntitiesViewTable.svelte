<script>
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {createEventDispatcher} from "svelte";
    import RelatedEntityAddNodesPanel from "./RelatedEntityAddNodesPanel.svelte";
    import RelatedEntityAddFlowsPanel from "./RelatedEntityAddFlowsPanel.svelte";
    import EntityLabel from "../../../../common/svelte/EntityLabel.svelte";

    export let diagramId;
    export let canEdit;
    export let measurables;
    export let changeInitiatives;
    export let datatypes;

    const dispatch = createEventDispatcher();

    const Modes = {
        OVERVIEW: "OVERVIEW",
        RELATED_ENTITY_ADD_NODES: "RELATED_ENTITY_ADD_NODES",
        RELATED_ENTITY_ADD_FLOWS: "RELATED_ENTITY_ADD_FLOWS",
    }

    let activeMode = Modes.OVERVIEW
    let selectedEntity = null;
    let activeGroup;

    function removeEntity(evt) {
        dispatch("removeEntity", evt.detail);
        selectedEntity = null;
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

    $: groups = [
        {
            name: "Viewpoints",
            kind: "MEASURABLE",
            getItems: () => measurables
        }, {
            name: "Change Initiatives",
            kind: "CHANGE_INITIATIVE",
            getItems: () => changeInitiatives
        }, {
            name: "Data Types",
            kind: "DATA_TYPE",
            getItems: () => datatypes
        }
    ];

    $: breadcrumbs = _.compact([
        {name: "Relationships", active: ! activeGroup, onClick: () => { activeGroup = null; selectedEntity = null;}},
        activeGroup
            ? {name: activeGroup.name, active: ! selectedEntity, onClick: () => selectedEntity = null}
            : null,
        selectedEntity
            ? {name: selectedEntity.data.name, active: true}
            : null,
    ]);

</script>

<div class="help-block">
    Related entities can be used to link this diagram to other pages in waltz.
    They can also be used to quickly add associated nodes and flows to diagrams or populate overlay groups.
</div>

<ol class="breadcrumb">
    {#each breadcrumbs as crumb}
        {#if crumb.active}
            <li class="active">{crumb.name}</li>
        {:else}
            <li>
                <a class="clickable"
                   on:click={crumb.onClick}>
                    {crumb.name}
                </a>
            </li>
        {/if}
    {/each}
</ol>

{#if !activeGroup}
    <ul class="list-unstyled"
        style="padding-left: 1em;">
        {#each groups as group}
        <li>
            <button class="btn-skinny"
                    on:click={() => activeGroup = group}>
                {group.name}
                <span class={_.isEmpty(group.getItems())
                        ? 'list-size-badge empty-list-badge'
                        : 'list-size-badge non-empty-list-badge'}>
                    {_.size(group.getItems())}
                </span>
            </button>
        </li>
        {/each}
    </ul>
{/if}

{#if activeGroup && ! selectedEntity}
    <div class:waltz-scroll-region-250={_.size(activeGroup.getItems()) > 8}>
        <ul class="list-unstyled">
            {#each activeGroup.getItems() as item}
                <li>
                    <button class="btn-skinny"
                            on:click={() => selectEntity(item)}>
                        <Icon name="fw"/>
                        <EntityLabel ref={item.data}/>
                    </button>
                </li>
            {:else}
                <li>
                    <Icon name="fw" />
                    No associated {activeGroup.name}s
                </li>
            {/each}
            {#if canEdit}
                <li style="border-top: 1px dotted #eee; padding-top: 0.2em; margin-top: 0.2em">
                    <Icon name="fw"/>
                    <button class="btn btn-skinny"
                            on:click={() => addEntity(activeGroup.kind)}>
                        <Icon name="plus"/>
                        Add
                    </button>
                </li>
            {/if}
        </ul>
    </div>
{/if}

{#if selectedEntity}
    <div style="padding-left: 1em;">
        {#if activeMode === Modes.RELATED_ENTITY_ADD_NODES}
        <RelatedEntityAddNodesPanel entity={selectedEntity}
                                    {canEdit}
                                    on:remove={removeEntity}/>
        {:else if activeMode === Modes.RELATED_ENTITY_ADD_FLOWS}
        <RelatedEntityAddFlowsPanel entity={selectedEntity}
                                    {canEdit}
                                    on:remove={removeEntity}/>
        {/if}
    </div>
{/if}


<style>
    .breadcrumb {
        margin-bottom: 0.4em;
    }

    .list-size-badge {
        padding-left: 0.4em;
        padding-right: 0.4em;
        border-radius: 2px;
    }

    .empty-list-badge {
        background-color: #e8e3e3;
    }

    .non-empty-list-badge {
        background-color: #d1e5d1;
    }
</style>

