<script>
    import _ from "lodash";
    import {getContext} from "svelte";
    import TargetAppCostOverlayCell from "./TargetAppCostOverlayCell.svelte";

    const overlayData = getContext("overlayData");

    $: maxCost = _
        .chain($overlayData)
        .map(d => [d.currentStateCost, d.targetStateCost])
        .flatten()
        .max()
        .value();

    $: cellDataByCellExtId = _.keyBy(
        $overlayData,
        d => d.cellExternalId);
</script>

{#each Object.entries(cellDataByCellExtId) as [key, cellData]}
    <h4>{key}</h4>
    <div class="overlay-cell {key}"
         data-cell-id={key}>
        <TargetAppCostOverlayCell {cellData}
                                  {maxCost}/>
    </div>
{/each}
