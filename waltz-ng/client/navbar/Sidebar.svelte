<script>
    import {availableSections, sidebarExpanded} from "./sidebar-store";
    import Icon from "../common/svelte/Icon.svelte";

    $: console.log({sections: $availableSections})
</script>

<ul class="list-unstyled">
{#each $availableSections as section}
    <li class="sidenav">
        <a class="clickable no-overflow">
            <Icon size="lg"
                  name={section.icon}/>
            <span class="section-name "
                  style={`opacity: ${$sidebarExpanded ? 1 : 0}`}>
                {section.name}
            </span>
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

    /* The navigation menu links */
    .sidenav a {
        text-decoration: none;
        font-size: 18px;
        color: $navbar-default-link-color;
        padding-bottom: 1em;
        padding-left: 1.8em;
        display: inline-block;
    }

    .section-name {
        transition: opacity ease-in-out 0.3s;
    }

    /* When you mouse over the navigation links, change their color */
    .sidenav a:hover {
        color: $navbar-default-link-hover-color;
    }
</style>