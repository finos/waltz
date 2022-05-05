<script>

    import {getContext} from "svelte";
    import BackingEntitiesOverlayCell from "./BackingEntitiesOverlayCell.svelte";
    import _ from "lodash";

    const overlayData = getContext("overlayData");


    $: cellDataByCellExtId = _.keyBy(
        $overlayData,
        d => d.cellExternalId);


</script>

{#each Object.entries(cellDataByCellExtId) as [key, cellData]}
    <h4>
        {key}
    </h4>
    <div class="overlay-cell {key}"
         data-cell-id={key}>
        <BackingEntitiesOverlayCell {cellData}/>
    </div>
{/each}
