<script>
    import {assessmentRenderMode, AssessmentRenderModes} from "../overlay-store";
    import BarGraphCell from "./BarGraph.svelte";
    import BoxGraphCell from "./BoxGraph.svelte";

    export let cellData = [];
    export let maxCount = 0;
    export let maxRatings = 0;
    export let renderMode;
    export let height;
    export let width;

    $: comp = $assessmentRenderMode === AssessmentRenderModes.BAR
        ? BarGraphCell
        : BoxGraphCell;

</script>

<div class="content">
    <svelte:component this={comp}
                      {cellData}
                      {maxCount}
                      {maxRatings}
                      {renderMode}
                      {width}
                      {height}/>
</div>
