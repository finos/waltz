<script>
    import {availableSections, sidebarExpanded} from "./sidebar-store";
    import Icon from "../common/svelte/Icon.svelte";

    $: console.log({sections: $availableSections})
</script>

<ul class="list-unstyled">
{#each $availableSections as section}
    <li class="sidenav">
        <a class="no-overflow">
            <Icon size="lg"
                  name={section.icon}/>
            {#if $sidebarExpanded}
                {section.name}
            {/if}
        </a>
    </li>
{/each}
</ul>

<a class="expansion-toggle"
   on:click={() => $sidebarExpanded = !$sidebarExpanded}>
    <Icon size="lg"
          name={$sidebarExpanded ? 'angle-double-left' : 'angle-double-right'}>
    </Icon>
</a>

<style type="text/scss">
    @import "style/_variables";

    .expansion-toggle {
        font-size: 64px;
        width: 100%;
        display: inline-block;
        text-align: right;

        transition: color ease-in-out 0.3s;

        color: $waltz-blue;
        &:hover {
            color: $waltz-blue-background;;
        }
    }
</style>