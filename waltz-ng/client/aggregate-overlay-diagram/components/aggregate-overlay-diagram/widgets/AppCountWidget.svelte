<script>
    export let cellData = {};
</script>


<svg class="content" viewBox="0 0 270 90">
    <foreignObject width="270" height="90">
        {#if cellData}
            <div style="font-size: 22px; width: 100%; text-align: center">
                App Count: #{cellData?.currentStateCount} &raquo; #{cellData?.targetStateCount}
            </div>
            <div style="font-size: 16px; width: 100%; text-align: center">
                Change: {cellData?.targetStateCount - cellData?.currentStateCount}
            </div>
        {:else}
            -
        {/if}
    </foreignObject>
</svg>
