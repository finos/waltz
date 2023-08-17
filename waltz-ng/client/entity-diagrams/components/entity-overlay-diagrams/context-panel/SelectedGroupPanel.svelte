<script>

    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import Item from "../Item.svelte";
    import {
        RenderModes
    } from "../../../../aggregate-overlay-diagram/components/aggregate-overlay-diagram/aggregate-overlay-diagram-utils";
    import {diagramService} from "../entity-diagram-store";
    import Icon from "../../../../common/svelte/Icon.svelte";

    const {selectedGroup, overlayData} = diagramService;

    $: cellData = $overlayData[$selectedGroup?.id];

</script>



{#if $selectedGroup}

    <div>
        {#if $selectedGroup.data}
            {$selectedGroup.title} (<EntityLink ref={$selectedGroup.data.entityReference}/>)
        {:else }
            {$selectedGroup.title}
        {/if}
    </div>

    <div class="waltz-scroll-region-250"
         style="margin-top: 1em; overflow-y: scroll">
        <Item data={$selectedGroup.data}
              cellId={$selectedGroup.id}
              height={$selectedGroup.props.minWidth / 3}
              width={$selectedGroup.props.minWidth}
              renderMode={RenderModes.FOCUSED}/>
    </div>

{:else}
    <div class="help-block">
        <Icon name="info-circle"/> Select a group from the diagram for more information
    </div>
{/if}
