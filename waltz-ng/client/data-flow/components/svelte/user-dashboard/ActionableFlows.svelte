<script>
import Grid from "../../../../common/svelte/Grid.svelte";

export let userName;
export let flows = [];
</script>

<div>
    <Grid>
    </Grid>

    {#if flows.length !== 0}
        Hello!
    {:else }
        No data flows found for {userName}
    {/if}
</div>