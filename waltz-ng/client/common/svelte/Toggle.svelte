

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
    <a class="clickable no-text-select"
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
    </a>
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
