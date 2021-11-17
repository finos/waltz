<script>

    import _ from "lodash";
    export let response;

    $: items = _.get(response, ["listResponse"], []);

</script>

{#if !_.isEmpty(items)}
    <ul>
        {#each items as item}
            <li>{item}</li>
        {/each}
    </ul>
{:else}
    <span class="text-muted">-</span>
{/if}


<style>

    ul {
        padding-left: 1em;
    }

</style>