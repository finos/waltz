<script>

    import _ from "lodash";
    import EntityLink from "../../common/svelte/EntityLink.svelte";
    import {groups, hoveredGroupId, movingGroup, selectedGroup} from "./diagram-builder-store";
    import {flip} from 'svelte/animate';
    import {flattenChildren} from "../../common/hierarchy-utils";
    import toasts from "../../svelte-stores/toast-store";

    export let group;

    function drop(evt, targetGroup) {
        evt.preventDefault();

        const children = flattenChildren($movingGroup);
        const childIds = _.map(children, d => d.id);

        if(targetGroup.id === $movingGroup.id || _.includes(childIds, targetGroup.id)) {
            toasts.warning("Cannot move a group to itself or one of it's children");

        } else {
            const moveGroup = _.find($groups, d => d.id === $movingGroup.id);
            const updatedGroup = Object.assign({}, moveGroup, {parentId: targetGroup.id})
            const withoutGroup = _.reject($groups, d => d.id === $movingGroup.id);
            $groups = _.concat(withoutGroup, updatedGroup);
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

        const workingGroups = _.reject($groups, d => d.parentId === targetGroup.parentId);
        $groups = _.concat(workingGroups, ...reorderedSiblings);
        clearDrag();
    }


    function clearDrag() {
        $movingGroup = null
        $hoveredGroupId = null;
    }

    function dragStart(evt, group) {
        $movingGroup = group;
    }

    function dragEnter() {
        $hoveredGroupId = group.id;
    }

    function dragEnterParent() {
        $hoveredGroupId = group.parentId;
    }

    $: console.log({group});

    $: showReorderPanels = $movingGroup
        && $movingGroup.parentId === group.parentId
        && $movingGroup.id !== group.id

</script>

<div style="height: 100%; width: 100%"
     draggable={true}
     on:dragstart|stopPropagation={event => dragStart(event, group)}
     ondragover="return false">

    <div style="display: flex; width: 100%">
        {#if showReorderPanels}
            <div class="reorder-box"
                 class:hovered={$hoveredGroupId === group.parentId}
                 on:dragenter|stopPropagation={dragEnterParent}
                 on:drop|stopPropagation={event => dropReorder(event, group, 0)}>
            </div>
        {/if}

        <div style="flex: 1 1 80%"
             on:dragenter|stopPropagation={dragEnter}
             on:drop|stopPropagation={event => drop(event, group)}>

            {#if group.props.showTitle}
                <div class="diagram-title clickable"
                     class:hovered={$hoveredGroupId === group.id}>
                    <button class="btn btn-plain"
                            on:click={() => $selectedGroup = group}>
                        {group.title}
                    </button>
                </div>
            {/if}

            <div class={`diagram-container diagram-container-${group.props.flexDirection}`}
                 class:hovered={$hoveredGroupId === group.id}>
                {#each _.orderBy(group.children, d => d.position) as child (child.id)}
                    <div class="group"
                         style={`flex: ${child.props.proportion} 1 ${_.floor(100 / (group.props.bucketSize + 1))}%;`}
                         animate:flip="{{duration: 300}}">
                        <svelte:self group={child}>
                        </svelte:self>
                    </div>
                {:else}
                    {#if group.data}
                        <div class="item">
                            <EntityLink ref={group.data}/>
                        </div>
                    {/if}
                {/each}
            </div>
        </div>


        {#if showReorderPanels}
            <div class="reorder-box"
                 class:hovered={$hoveredGroupId === group.parentId}
                 on:dragenter|stopPropagation={dragEnterParent}
                 on:drop|stopPropagation={event => dropReorder(event, group, 1)}>
            </div>
        {/if}

    </div>

</div>


<style type="text/scss">

    .diagram-container {

        display: flex;
        flex-wrap: wrap;
        justify-content: space-evenly;
        gap: 0.5em;

        border: 1px solid #000d79;
        background-color: #f6f7ff;

        height: fit-content;
        min-height: 5em;
    }

    .diagram-container.hovered {
        &:hover {
            background-color: lighten(#f6f7ff, 20%);
        }
    }

    .diagram-container-row {
        flex-direction: row;
        align-items: flex-start;
        align-content: flex-start;
    }

    .diagram-container-row > .group {
        //flex: 1 1 25%; /* when rows this sets the width*/
        height: fit-content;
        min-height: 5em;
    }

    .diagram-container-row > .item {
        flex: 0 1 10em; /* when rows this sets the width*/
        height: fit-content;
        min-height: 5em;
    }

    .diagram-container-column {
        flex-direction: column;
        align-items: center;
        align-content: center;
        max-height: 60em;
    }

    .diagram-container-column > .group {
        flex: 1 1 45%; /* when columns this sets the height*/
        /*width: fit-content;*/
        min-width: 10em;
    }

    .diagram-container-column > .item {
        flex: 1 1 5em; /* when columns this sets the height*/
        width: fit-content;
        min-width: 10em;
    }

    .item {
        border: 1px solid #000d79;
        background-color: #d7f4fa;
        margin: 0.5em;
        padding: 0.25em;
    }

    .group {
        /*border: 1px solid purple;*/
        /*background-color: #e2efff;*/
        margin: 0.5em;
    }

    .diagram-title {
        text-align: center;
        border: 1px solid #000d79;
        background-color: #000d79;
        font-weight: bolder;
        color: white;
        padding: 0 0.5em;

        &:hover {
            background-color: lighten(#000d79, 50%);
        }

        button {
            width: 100%;
            outline: none !important;
            color: white;
        }
    }

    .diagram-title.hovered {
        background-color: lighten(#000d79, 50%);
    }

    .reorder-box {
        background-color: #f6f7ff;
        flex: 1 1 10%;
        border:  1px dotted #000d79;

        &:hover {
            background-color: lighten(#f6f7ff, 20%);
        }
    }

    .reorder-box.hovered {
        //&:hover {
            background-color: lighten(#f6f7ff, 20%);
        //}
    }

</style>