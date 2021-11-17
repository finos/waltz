<script>

    import {timeFormat} from "d3-time-format";

    export let response;

    const dateFormat = timeFormat("%Y-%m-%d");

</script>

{#if response?.dateResponse}
    <span>{dateFormat(new Date(response?.dateResponse))}</span>
{:else}
    <span class="text-muted">-</span>
{/if}