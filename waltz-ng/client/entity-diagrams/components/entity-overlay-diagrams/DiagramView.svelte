<script>

    import _ from "lodash";
    import {diagramService, hoveredGroupId} from "./entity-diagram-store";
    import {flip} from 'svelte/animate';
    import Item, {cellId} from "./Item.svelte";
    import {
        mkContainerStyle,
        mkContentBoxStyle,
        mkGroupStyle,
        mkItemStyle,
        mkTitleStyle
    } from "./entity-diagram-utils";
    import Icon from "../../../common/svelte/Icon.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";

    export let group;

    const {selectedOverlay, overlayData} = diagramService;

    $: cellData = _.get($overlayData, group.id);

    $: children = _.filter(group.children, d => _.includes(_.keys($overlayData), d.id));

</script>

{#if group}
<div>
    <div style="display: flex">
        <div style={mkContentBoxStyle(group)}>
            {#if group.props.showTitle}
                <div style={mkTitleStyle(group, $hoveredGroupId)}>
                    {#if group.data}
                        <EntityLink ref={group.data}
                                    showIcon="false"
                                    isSecondaryLink="true">
                            <span>
                                {group.title}
                                <Icon name="external-link"/>
                            </span>
                        </EntityLink>
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
                        <div style={mkItemStyle(group)}>
                            <Item data={group.data}
                                  cellId={group.id}/>
                        </div>
                    {/if}
                {/each}
            </div>
        </div>
    </div>
</div>
{/if}