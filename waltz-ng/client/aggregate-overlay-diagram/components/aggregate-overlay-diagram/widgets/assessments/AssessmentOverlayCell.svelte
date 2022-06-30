<script>
    import {assessmentRenderMode, Modes} from "./store";
    import BarGraphCell from "./BarGraphCell.svelte";
    import BoxGraphCell from "./BoxGraphCell.svelte";

    export let cellData = [];
    export let maxCount = 0;
    export let renderMode;

    $: comp = $assessmentRenderMode === Modes.BAR
        ? BarGraphCell
        : BoxGraphCell;

</script>

<div class="content">
    <svelte:component this={comp}
                      {cellData}
                      {maxCount}
                      {renderMode}/>
</div>
