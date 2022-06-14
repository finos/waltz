<script>
    import {renderMode, Modes} from "./store";
    import BarGraphCell from "./BarGraphCell.svelte";
    import BoxGraphCell from "./BoxGraphCell.svelte";

    export let cellData = [];
    export let maxCount = 0;

    $: comp = $renderMode === Modes.BAR
        ? BarGraphCell
        : BoxGraphCell;

</script>

<div class="content">
    <svelte:component this={comp}
                      {cellData}
                      {maxCount}/>
</div>
