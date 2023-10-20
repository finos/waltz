<script>

    import _ from "lodash";
    import {diagramService, hoveredGroupId} from "../entity-diagram-store";
    import {flip} from 'svelte/animate';
    import CellContent from "../CellContent.svelte";
    import {
        mkCellContentStyle,
        mkChildGroupStyle,
        mkContentBoxStyle,
        mkGroupCellStyle,
        mkTitleStyle
    } from "../entity-diagram-utils";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";

    export let group;

    const {selectedOverlay, overlayData} = diagramService;

    $: cellData = _.get($overlayData, group?.id);

    $: children = group?.children || [];

</script>

{#if group}
<div>
    <div style="display: flex">
        <div style={mkGroupCellStyle(group)}>
            {#if group.props.showTitle}
                <div style={mkTitleStyle(group, $hoveredGroupId)}>
                    {#if group.data}
                        <EntityLink ref={group.data.entityReference}
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

            <div style={mkContentBoxStyle(group)}>
                {#each _.orderBy(children, d => d.position) as child (child.id)}
                        <div style={mkChildGroupStyle(group, child)}
                             animate:flip="{{duration: 300}}">
                                <svelte:self group={child}>
                                </svelte:self>
                        </div>
                {:else}
                    {#if group.data}
                        <div style={mkCellContentStyle(group, $hoveredGroupId)}>
                            <CellContent data={group.data}
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