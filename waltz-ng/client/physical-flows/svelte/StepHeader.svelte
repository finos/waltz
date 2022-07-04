<script>

import Check from "./Check.svelte";
import Icon from "../../common/svelte/Icon.svelte";

export let checked = false;
export let label;
export let icon;
</script>


<h3>
    <Check selected={checked}/>
    {#if icon}
        <Icon name={icon} fixedWidth="true"/>
    {/if}
    {label}
</h3>