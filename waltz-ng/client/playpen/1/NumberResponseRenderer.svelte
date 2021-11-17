<script>
    import _ from "lodash";

    export let response;

</script>

{#if !_.isNil(response?.numberResponse)}
    <span>{response?.numberResponse}</span>
{:else}
    <span class="text-muted">-</span>
{/if}
