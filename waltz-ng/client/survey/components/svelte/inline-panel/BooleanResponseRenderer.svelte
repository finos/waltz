<script>
    import _ from "lodash";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {stringToBoolean} from "../../../../common/string-utils";

    export let response;

    $: booleanResponse = _.isNil(response?.booleanResponse) || response?.booleanResponse === "null"
        ? null
        : stringToBoolean(response?.booleanResponse);

</script>

{#if !_.isNil(booleanResponse)}
    {#if booleanResponse}
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