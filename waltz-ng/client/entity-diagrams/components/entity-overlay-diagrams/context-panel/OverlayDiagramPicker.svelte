<script>

    import {diagramService, selectionOptions} from "../entity-diagram-store";
    import {hideEmptyCells} from "../entity-diagram-store";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import Toggle from "../../../../common/svelte/Toggle.svelte";
    import {overlays} from "../entity-diagram-utils";
    import _ from "lodash";


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

</script>



{#if overlayMode === OverlayModes.VIEW && $selectedOverlay}

        <h4>
            <Icon name={$selectedOverlay.icon}/>
            {$selectedOverlay.name}
            <button class="btn btn-skinny small"
                    on:click={() => overlayMode = OverlayModes.PICKER}>
                (Change overlay)
            </button>
        </h4>

        <Toggle labelOn="Hiding cells with no data"
                labelOff="Showing all cells"
                state={$hideEmptyCells}
                onToggle={() => $hideEmptyCells = !$hideEmptyCells}/>

        <svelte:component this={$selectedOverlay.parameterWidget}/>


{:else if overlayMode === OverlayModes.PICKER || _.isEmpty($selectedOverlay)}
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