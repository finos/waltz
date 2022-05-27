<script>

    import {cellHeight} from "./overlay-diagram-builder-store";
    import {statsBoxWidth} from "./overlay-diagram-builder-store";

</script>


<span>CellHeight: {$cellHeight}</span>

<input id="cellHeight"
       type="range"
       min="100"
       max="300"
       bind:value={$cellHeight}>


<span>StatsBoxWidth: {$statsBoxWidth}</span>

<input id="statsBoxWidth"
       type="range"
       min="50"
       max="300"
       bind:value={$statsBoxWidth}>