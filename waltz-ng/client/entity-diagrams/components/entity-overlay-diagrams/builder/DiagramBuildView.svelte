<script>

    import _ from "lodash";
    import {diagramService, hoveredGroupId} from "../entity-diagram-store";
    import {movingGroup} from "./diagram-builder-store";
    import {flip} from 'svelte/animate';
    import {flattenChildren} from "../../../../common/hierarchy-utils";
    import toasts from "../../../../svelte-stores/toast-store";
    import Item from "../Item.svelte";
    import {
        mkContainerStyle,
        mkContentBoxStyle,
        mkGroupStyle,
        mkItemStyle,
        mkReorderBoxStyle,
        mkTitleStyle
    } from "../entity-diagram-utils";

    export let group;

    let timeout;
    let showReorderPanels = false;

    const {selectedOverlay, updateGroup, updateChildren, selectedGroup, groups, selectGroup} = diagramService;

    function drop(evt, targetGroup) {
        evt.preventDefault();

        const children = flattenChildren($movingGroup);
        const childIds = _.map(children, d => d.id);

        if(targetGroup.id === $movingGroup.id || _.includes(childIds, targetGroup.id)) {
            toasts.warning("Cannot move a group to itself or one of it's children");

        } else {
            const moveGroup = _.find($groups, d => d.id === $movingGroup.id);
            const updatedGroup = Object.assign({}, moveGroup, {parentId: targetGroup.id})
            updateGroup(updatedGroup);
        }
        clearDrag();
    }

    function dropReorder(evt, targetGroup, positionOffset) {
        evt.preventDefault();

        const newGroupPosition = targetGroup.position + positionOffset;

        const reorderedSiblings = _
            .chain($groups)
            .filter(d => d.parentId === targetGroup.parentId)
            .map(d => {

                const position = d.id === $movingGroup.id
                    ? newGroupPosition
                    : d.position < newGroupPosition
                        ? d.position
                        : d.position + 1;

                return Object.assign({}, d, { position })
            })
            .value();

        updateChildren(targetGroup.parentId, reorderedSiblings);
        clearDrag();
    }

    function clearDrag() {
        $movingGroup = null
        stopHover();
    }

    function dragStart(evt, group) {
        $movingGroup = group;
    }

    function dragEnter() {
        startHover(group.id);
    }

    function dragEnterParent() {
        startHover(group.parentId)
    }

    $: showReorderPanels = !_.isNull($movingGroup)
        && $movingGroup.parentId === group.parentId
        && $movingGroup.id !== group.id


    function startHover(groupId) {
        timeout = setTimeout(function () {
            $hoveredGroupId = groupId
        }, 300);
    }

    function stopHover() {
        clearTimeout(timeout);
        $hoveredGroupId = null;
    }

    function selectOverlayGroup(group) {
        selectGroup(group);
    }

</script>

{#if group}
<div draggable={true}
     on:dragstart|stopPropagation={event => dragStart(event, group)}
     ondragover="return false">

    <div style="display: flex">

        {#if showReorderPanels}
            <div style={mkReorderBoxStyle(group)}
                 on:dragenter|stopPropagation={dragEnterParent}
                 on:drop|stopPropagation={event => dropReorder(event, group, 0)}>
            </div>
        {/if}

        <div style={mkContentBoxStyle(group)}
             on:dragenter|stopPropagation={dragEnter}
             on:drop|stopPropagation={event => drop(event, group)}>

            {#if group.props.showTitle}
                <div style={mkTitleStyle(group, $hoveredGroupId)}
                     on:mouseover|stopPropagation={() => startHover(group.id)}
                     on:mouseout={stopHover}
                     on:focus|stopPropagation={() => startHover(group.id)}
                     on:blur|stopPropagation={stopHover}>
                    <button style="outline: none !important; width: 100%; background: none; border: none; color: inherit;"
                            on:click={() => selectOverlayGroup(group)}>
                        {group.title}
                    </button>
                </div>
            {/if}

            <div style={mkContainerStyle(group)}>
                {#each _.orderBy(group.children, d => d.position) as child (child.id)}
                    <div style={mkGroupStyle(group, child)}
                         animate:flip="{{duration: 300}}">
                        <svelte:self group={child}>
                        </svelte:self>
                    </div>
                {:else}
                    {#if group.data}
                        <div style={mkItemStyle(group)}>
                            <Item data={group.data}
                                  cellId={group.id}
                                  height={group.props.minWidth / 3}
                                  width={group.props.minWidth}/>
                        </div>
                    {/if}
                {/each}
            </div>
        </div>

        {#if showReorderPanels}
            <div style={mkReorderBoxStyle(group)}
                 on:dragenter|stopPropagation={dragEnterParent}
                 on:drop|stopPropagation={event => dropReorder(event, group, 1)}>
            </div>
        {/if}

    </div>

</div>
{/if}