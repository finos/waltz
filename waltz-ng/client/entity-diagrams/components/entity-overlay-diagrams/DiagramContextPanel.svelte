<script>

    import {overlays} from "./entity-diagram-utils";
    import {diagramService, selectionOptions} from "./entity-diagram-store";
    import Icon from "../../../common/svelte/Icon.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";

    const ContextModes = {
        VIEW: "VIEW",
        OVERLAY: "OVERLAY"
    }

    const OverlayModes = {
        VIEW: "VIEW",
        PICKER: "PICKER"
    }

    let overlayMode = OverlayModes.VIEW;

    const {selectedDiagram, selectedOverlay, selectedGroup, selectOverlay, reset} = diagramService;

    function selectDiagramOverlay(overlay) {
        selectOverlay(overlay, $selectionOptions);
        overlayMode = OverlayModes.VIEW;
    }

    function clearSelectedDiagram() {
        reset();
    }

</script>


{#if $selectedDiagram}
    <h4>
        {$selectedDiagram.name}
    </h4>
    {#if $selectedDiagram.description}
        <div class="help-block">
            {$selectedDiagram.description}
        </div>
    {/if}

    <button class="btn btn-default"
            on:click={clearSelectedDiagram}>
        Back to Diagram Picker
    </button>
{:else}
    <div class="help-block">
        <Icon name="info-circle"/> Select a diagram from the list.
    </div>
{/if}

<hr>

{#if overlayMode === OverlayModes.VIEW}
    <h4>
        <Icon name={$selectedOverlay.icon}/>
        {$selectedOverlay.name}
        <button class="btn btn-skinny small"
                on:click={() => overlayMode = OverlayModes.PICKER}>
            (Change overlay)
        </button>
    </h4>

    <svelte:component this={$selectedOverlay.parameterWidget}/>

{:else if overlayMode === OverlayModes.PICKER}
    <div class="help-block">
        <Icon name="info-circle"/>
        Select an overlay from the list below
    </div>

    <table class="table table-condensed small">
        <tbody>
        {#each overlays as overlay}
            <tr class="clickable"
                on:click={() => selectDiagramOverlay(overlay)}>
                <td>
                    {overlay.name}
                </td>
                <td>
                    {overlay.description}
                </td>
            </tr>
        {/each}
        </tbody>
    </table>
{/if}

{#if $selectedGroup}
    <hr>
    {$selectedGroup.title}
    {#if $selectedGroup.data}
        <div>
            <EntityLink ref={$selectedGroup.data.entityReference}/>
        </div>
    {:else}
        <div class="help-block">No data has been linked to this group</div>
    {/if}

{/if}
