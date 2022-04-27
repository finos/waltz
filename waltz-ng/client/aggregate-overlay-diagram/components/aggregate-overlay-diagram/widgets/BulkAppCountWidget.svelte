<script>
    import _ from "lodash";
    import {getContext} from "svelte";
    import AppCountOverlayCell from "./AppCountOverlayCell.svelte";

    const overlayData = getContext("overlayData");

    $: maxCount = _
         .chain($overlayData)
         .map(d => [d.currentStateCount, d.targetStateCount])
         .flatten()
        .max()
        .value();

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
        <AppCountOverlayCell {cellData}
                             {maxCount}/>
    </div>
{/each}
