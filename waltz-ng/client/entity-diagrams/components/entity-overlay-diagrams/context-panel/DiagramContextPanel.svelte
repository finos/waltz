<script>

    import {diagramService} from "../entity-diagram-store";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import OverlayDiagramPicker from "./OverlayDiagramPicker.svelte";
    import SelectedGroupPanel from "./SelectedGroupPanel.svelte";

    const {selectedDiagram, reset} = diagramService;

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

<OverlayDiagramPicker/>

<hr>

<SelectedGroupPanel/>