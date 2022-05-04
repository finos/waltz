<script>
    import _ from "lodash";
    import {getContext} from "svelte";
    import AssessmentOverlayCell from "./AssessmentOverlayCell.svelte";

    const overlayData = getContext("overlayData");

    $: maxCount = _
        .chain($overlayData)
        .map(d => d.counts)
        .flatten()
        .map(d => d.count)
        .max()
        .value();

    $: cellDataByCellExtId = _.keyBy($overlayData, d => d.cellExternalId);
</script>

{#each Object.entries(cellDataByCellExtId) as [key, cellData]}
    <h4>{key}</h4>
    <div class="overlay-cell {key}"
         data-cell-id={key}>
        <AssessmentOverlayCell {cellData}
                               {maxCount}/>
    </div>
{/each}
