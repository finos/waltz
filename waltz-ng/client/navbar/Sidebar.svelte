<script>
    import {sidebarExpanded} from "./sidebar-store";
    import {availableSections, activeSections} from "../dynamic-section/section-store";
    import Icon from "../common/svelte/Icon.svelte";

    function activateSection(section) {
        activeSections.add(section);
    }
</script>

<div class={$sidebarExpanded ? "sidebar-expanded" : "sidebar-collapsed" }>
    <ul class="list-unstyled">
        {#each $availableSections as section}
            <li class="sidenav">
                <button class="btn-skinny no-overflow"
                        on:click={() => activateSection(section)}>
                    <Icon size="lg"
                          name={section.icon}/>
                    <span class="section-name "
                          style={`opacity: ${$sidebarExpanded ? 1 : 0}`}>
                        {section.name}
                    </span>
                </button>
                {#if section.children}
                    <ul class="child-list list-unstyled">
                        {#each section.children as child}
                            <li class="sidenav">
                                <button class="btn-skinny no-overflow"
                                        on:click={() => activateSection(child)}>
                                    <Icon size="lg"
                                          name={child.icon}/>
                                    <span class="section-name "
                                          style={`opacity: ${$sidebarExpanded ? 1 : 0}`}>
                                        {child.name}
                                    </span>
                                </button>
                            </li>
                        {/each}
                    </ul>
                {/if}
            </li>
        {/each}
    </ul>
</div>

<button class="btn-skinny expansion-toggle"
   style="margin-bottom: 1em"
   on:click={() => $sidebarExpanded = !$sidebarExpanded}>
    <Icon size="lg"
          name={$sidebarExpanded ? 'angle-double-left' : 'angle-double-right'}>
    </Icon>
</button>


<style type="text/scss">
    @import "style/_variables";

    .child-list {
        transition: transform ease-in-out 1s;
    }

    .sidebar-expanded .child-list {
        transform: translateX(18px);
    }

    .sidebar-collapsed .child-list {
        transform: translateX(0px);
    }

    .expansion-toggle {
        font-size: 64px;
        width: 100%;
        display: inline-block;
        text-align: right;
        transition: color ease-in-out 0.3s;
        transform: translateX(-10px);

        color: $waltz-blue;
        &:hover {
            color: $waltz-blue-background;;
        }
    }

    /* The navigation menu links */
    .sidenav button {
        text-decoration: none;
        font-size: $waltz-navigation-font-size;
        color: $navbar-default-link-color;
        padding-bottom: 1em;
        padding-left: 2em;
        display: inline-block;
    }

    .section-name {
        transition: opacity ease-in-out 0.3s;
    }

    /* When you mouse over the navigation links, change their color */
    .sidenav button:hover {
        color: $navbar-default-link-hover-color;
    }
</style>