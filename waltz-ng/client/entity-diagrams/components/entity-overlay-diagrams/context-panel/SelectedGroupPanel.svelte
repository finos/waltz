<script>

    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import CellContent from "../CellContent.svelte";
    import {
        RenderModes
    } from "../../../../aggregate-overlay-diagram/components/aggregate-overlay-diagram/aggregate-overlay-diagram-utils";
    import {diagramService} from "../entity-diagram-store";
    import Icon from "../../../../common/svelte/Icon.svelte";

    const {selectedGroup, overlayData, selectedOverlay} = diagramService;

    $: cellData = $overlayData[$selectedGroup?.id];

</script>



{#if $selectedGroup}

    {#if $selectedGroup.data}
        <h4>
            <EntityLink ref={$selectedGroup.data.entityReference}/>
        </h4>

        <div class="waltz-scroll-region-250"
             style="margin-top: 1em; overflow-y: scroll">
            <CellContent data={$selectedGroup.data}
                         cellId={$selectedGroup.id}
                         height={$selectedGroup.props.minWidth / 3}
                         width={$selectedGroup.props.minWidth}
                         renderMode={RenderModes.FOCUSED}/>
        </div>

        {#if $selectedOverlay?.legend}
            <svelte:component this={$selectedOverlay.legend}/>
        {/if}

    {:else}
        <div class="help-block">
            <Icon name="info-circle"/> This group has no backing entity
        </div>
    {/if}

{:else}
    <div class="help-block">
        <Icon name="info-circle"/> Select a group from the diagram for more information
    </div>
{/if}
