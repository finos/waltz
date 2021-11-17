<script>
    import _ from "lodash";
    import Icon from "../../common/svelte/Icon.svelte";

    export let response;

</script>

{#if !_.isNil(response?.booleanResponse)}
    {#if response?.booleanResponse}
        <span class="true-icon">
            <Icon name="check"/>
        </span>
        true
    {:else}
        <span class="false-icon">
            <Icon name="times"/>
        </span>
        false
    {/if}
{:else}
    <span class="text-muted">-</span>
{/if}

<style>

    .true-icon {
        color: #3c763d;
    }

    .false-icon {
        color: #a94442;
    }
</style>