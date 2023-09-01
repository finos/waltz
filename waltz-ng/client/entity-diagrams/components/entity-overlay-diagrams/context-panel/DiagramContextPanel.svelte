<script>

    import {diagramService} from "../entity-diagram-store";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import OverlayDiagramPicker from "./OverlayDiagramPicker.svelte";
    import SelectedGroupPanel from "./SelectedGroupPanel.svelte";
    import _ from "lodash";

    const {selectedDiagram, reset, selectedGroup, selectedOverlay, overlayParameters} = diagramService;

    function clearSelectedDiagram() {
        reset();
    }

</script>

{#if $selectedDiagram}
    <h4>
        {$selectedDiagram.name}
        <button class="btn btn-skinny small"
                on:click={clearSelectedDiagram}>
            Back to Diagram Picker
        </button>
    </h4>
    {#if $selectedDiagram.description}
        <div class="help-block">
            {$selectedDiagram.description}
        </div>
    {/if}
{:else}
    <div class="help-block">
        <Icon name="info-circle"/> Select a diagram from the list.
    </div>
{/if}

<details open={_.isEmpty($selectedOverlay) || _.isEmpty($overlayParameters)}>
    <summary>
        Overlays
    </summary>
    <OverlayDiagramPicker/>
</details>

<details open={!_.isEmpty($selectedGroup)}>
    <summary>
        Detail
    </summary>
    <SelectedGroupPanel/>
</details>