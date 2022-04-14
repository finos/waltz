<script>
    import {numberFormatter} from "../../../../common/string-utils";

    export let cellData = {};
</script>


<svg class="content" viewBox="0 0 270 90">
    <foreignObject width="270" height="90">
        {#if cellData}
            <div style="font-size: 20px; width: 100%; text-align: center">
                App Costs: {numberFormatter(cellData?.currentStateCost, 2)}
                &raquo; {numberFormatter(cellData?.targetStateCost, 2)}
            </div>
            <div style="font-size: 16px; width: 100%; text-align: center">
                Change: {numberFormatter(cellData?.targetStateCost - cellData?.currentStateCost, 2)}
            </div>

        {:else}
            -
        {/if}
    </foreignObject>
</svg>
