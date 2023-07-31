<script>

    import {overlays} from "./entity-diagram-utils";
    import {diagramService, selectionOptions} from "./entity-diagram-store";

    let contextModes = {
        VIEW: "VIEW",
        OVERLAY: "OVERLAY"
    }

    const {selectedOverlay, selectOverlay} = diagramService;


    function selectDiagramOverlay(overlay) {
        selectOverlay(overlay, $selectionOptions);
    }

</script>


<h4>Context Panel Here!</h4>

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