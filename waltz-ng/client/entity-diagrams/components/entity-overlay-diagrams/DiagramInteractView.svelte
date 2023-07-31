<script>

    import _ from "lodash";
    import {diagramService, hoveredGroupId} from "./entity-diagram-store";
    import {flip} from 'svelte/animate';
    import Item from "./Item.svelte";
    import {mkContainerStyle, mkContentBoxStyle, mkGroupStyle, mkItemStyle, mkTitleStyle} from "./entity-diagram-utils";

    export let group;
    export let parentEntityRef;

    const {selectedOverlay, selectedGroup, selectGroup} = diagramService;

    function selectOverlayGroup() {
        selectGroup(group);
    }

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
                            {group.title}
                        </button>
                    {:else}
                        {group.title}
                    {/if}
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
                            <Item {parentEntityRef} data={group.data}/>
                        </div>
                    {/if}
                {/each}
            </div>
        </div>
    </div>
</div>
{/if}