<script>

    import _ from "lodash";
    import {diagramService, hoveredGroupId, hideEmptyCells} from "./entity-diagram-store";
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


    function hasData(child) {
        return !_.isEmpty(child.overlayData)
            || _.some(flattenChildren(child.children), d => !_.isEmpty(d.overlayData));
    }

    $: children = $hideEmptyCells
        ? _.filter(group.children, child => hasData(child))
        : group.children;

</script>

{#if group}
<div>
    <div style="display: flex">
        <div style={mkContentBoxStyle(group)}>
            {#if $selectedOverlay.showTitle || group.props.showTitle}
                <div style={mkTitleStyle(group, $hoveredGroupId)}>
                    {#if group.data}
                        <button style="outline: none !important; width: 100%; background: none; border: none; color: inherit;"
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
                    {#if group.data}
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