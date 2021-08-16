<script>
    import {sidebarExpanded} from "./sidebar-store";
    import {activeSections, availableSections} from "../dynamic-section/section-store";
    import Icon from "../common/svelte/Icon.svelte";

    import _ from "lodash";

    function activateSection(section) {
        activeSections.add(section)
    }

    $: activeSectionIds = _.map($activeSections.sections, d => d.id);

</script>

<div class={$sidebarExpanded ? "sidebar-expanded" : "sidebar-collapsed" }>
    <ul class="list-unstyled">
        {#each $availableSections as section}
            <li class={_.includes(activeSectionIds, section.id) ? "selected-sidenav" : "sidenav"}>
                <button class="btn-skinny no-overflow"
                        class:selected={_.includes(activeSections.sections, section)}
                        on:click={() => activateSection(section)}>
                    <Icon size="lg"
                          style={`opacity: ${_.includes(activeSectionIds, section.id)} ? 1 : 0}`}
                          name={section.icon}/>
                    <span class="section-name"
                          style={`opacity: ${$sidebarExpanded ? 1 : 0}`}>
                        {section.name}
                    </span>
                </button>
                {#if section.children}
                    <ul class="child-list list-unstyled">
                        {#each section.children as child}
                            <li class={_.includes(activeSectionIds, child.id) ? "selected-sidenav" : "sidenav"}>
                                <button class="btn-skinny no-overflow"
                                        on:click={() => activateSection(child)}>
                                    <Icon size="lg"
                                          style={`opacity: ${_.includes(activeSectionIds, child.id)} ? 1 : 0}`}
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

      button {
        transition: padding-left ease-in-out 1s;
      }

    }

    .sidebar-expanded .child-list {

      .selected-sidenav button {
            padding-left: 3.75em;
      }

      .sidenav button {
            padding-left: 3.75em;
            border-left: $navbar-default-bg 0.25em solid;
            color: $navbar-default-link-color;
      }
    }

    .sidebar-collapsed .child-list {

      .sidenav button {
        border-left: $navbar-default-bg 0.25em solid;
        color: $navbar-default-link-color;
      }

    }

    .expansion-toggle {
        font-size: 60px;
        width: 100%;
        display: inline-block;
        text-align: right;
        transition: color ease-in-out 0.3s;
        transform: translateX(-10px);

        color: $waltz-blue;
        &:hover {
            color: $waltz-blue-background;
        }
    }

    /* The navigation menu links */
    .sidenav button {
        padding-top: 0.5em;
        text-align: center;
        text-decoration: none;
        font-size: $waltz-navigation-font-size;
        color: $navbar-default-link-color;
        padding-bottom: 0.5em;
        padding-left: 1.75em;
        display: inline-block;
        border-left: $navbar-default-bg 0.25em solid;

    }


    .selected-sidenav button {
            padding-top: 0.5em;
            text-decoration: none;
            font-size: $waltz-navigation-font-size;
            color: white;
            padding-bottom: 0.5em;
            padding-left: 1.75em;
            display: inline-block;
            border-left: white 0.25em solid;
    }


    .section-name {
        transition: opacity ease-in-out 0.3s;
    }

    /* When you mouse over the navigation links, change their color */
    .sidenav button:hover {
        color: $navbar-default-link-hover-color;
    }
</style>