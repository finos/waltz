<script>

    import _ from "lodash";
    import {diagramService, hoveredGroupId} from "./entity-diagram-store";
    import {flip} from 'svelte/animate';
    import Item from "./Item.svelte";
    import {mkContainerStyle, mkContentBoxStyle, mkGroupStyle, mkItemStyle, mkTitleStyle} from "./entity-diagram-utils";
    import {flattenChildren} from "../../../common/hierarchy-utils";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {entity} from "../../../common/services/enums/entity";

    export let group;

    let itemElem;

    const {selectedOverlay, selectedGroup, selectGroup, overlayData} = diagramService;

    function selectOverlayGroup() {
        selectGroup(group);
    }

    $: cellData = _.get($overlayData, group.id);

    $: flattenedChildren = flattenChildren(group);

    $: children = _.filter(group.children, child => !_.isEmpty(child.overlayData) || _.some(flattenChildren(child), d => !_.isEmpty(d.overlayData)));

</script>

{#if group}
<div>
    <div style="display: flex">
        <div style={mkContentBoxStyle(group)}>
            {#if group.props.showTitle}
                <div style={mkTitleStyle(group, $hoveredGroupId)}>
                    {#if group.data}
                        <button class="btn btn-plain"
                                on:click={selectOverlayGroup}>
                            <Icon name={_.get(entity, [group.data.entityReference.kind, "icon"], "info-circle")}/>
                            {group.title}
                        </button>
                    {:else}
                        {group.title}
                    {/if}
                </div>
            {/if}

            <div style={mkContainerStyle(group)}>
                {#each _.orderBy(children, d => d.position) as child (child.id)}
                    <div style={mkGroupStyle(group, child)}
                         animate:flip="{{duration: 300}}">
                        <svelte:self group={child}>
                        </svelte:self>
                    </div>
                {:else}
                    {#if group.data && !_.isEmpty(cellData)}
                        <div style={mkItemStyle(group)}
                             bind:this={itemElem}>
                            <Item data={group.data}
                                  cellId={group.id}
                                  height={group.props.minWidth / 3}
                                  width={group.props.minWidth}/>
                        </div>
                    {/if}
                {/each}
            </div>
        </div>
    </div>
</div>
{/if}