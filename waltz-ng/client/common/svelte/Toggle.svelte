<script>
    import Icon from "./Icon.svelte";

    export let iconOn = "toggle-on";
    export let iconOff = "toggle-off";
    export let labelOn;
    export let labelOff;
    export let readOnly;
    export let state;
    export let onToggle = () => console.log("toggle");

</script>


{#if !readOnly}
    <button class="btn btn-skinny no-text-select"
       style="text-decoration: none"
       on:click={() => onToggle()}>
        {#if state}
            <span>
                <Icon name={iconOn}/>
                <span>{labelOn}</span>
            </span>
        {:else }
            <span>
                <Icon name={iconOff}/>
                <span>{labelOff}</span>
            </span>
        {/if}
    </button>
{:else}
    {#if state}
        <span>
            <Icon name={iconOn}/>
            <span>{labelOn}</span>
        </span>
    {:else }
        <span>
            <Icon name={iconOff}/>
            <span>{labelOff}</span>
        </span>
    {/if}
{/if}

<style>
    button {
        font-size: inherit;
    }
</style>