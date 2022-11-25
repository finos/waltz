<script>

    import Check from "./Check.svelte";
    import Icon from "../../common/svelte/Icon.svelte";

    export let checked = false;
    export let label;
    export let icon;
    export let expanded;
    export let onToggleExpanded = () => console.log("toggling section")

</script>


<button class="btn-plain"
        on:click={onToggleExpanded}>
    <h3 class="clickable">
        <Icon name={expanded ? "chevron-down" : "chevron-right"}
              fixedWidth="true"/>

        <Check selected={checked}/>
        {#if icon}
            <Icon name={icon} fixedWidth="true"/>
        {/if}
        {label}
    </h3>
</button>

