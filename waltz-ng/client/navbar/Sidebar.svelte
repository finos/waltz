<script>
    import _ from "lodash";
    import {scaleLinear} from "d3-scale";

    import logoWithText from "../../images/branding/waltz_badge+text_horizontal_negative_bw_145px.png";
    import logoNoText from "../../images/branding/waltz_badge_negative_bw_32px.png";
    import Icon from "../common/svelte/Icon.svelte";
    import {sidebarExpanded} from "./sidebar-store";
    import {activeSections, availableSections} from "../dynamic-section/section-store";
    import {yellow} from "../common/colors";
    import {settingsStore} from "../svelte-stores/settings-store";
    import ViewLink from "../common/svelte/ViewLink.svelte";

    let jumpOffset = 250;

    function activateSection(section, offset) {
        activeSections.add(section);
        window.scrollTo(0, offset);
    }

    function calcJumpOffset(pageKind) {
        switch(pageKind){
            case "main.app.view":
                return 300;
            case "main.measurable.view":
            case "main.org-unit.view":
                return 500;
            case "main.process-diagram.view":
            case "main.flow-diagram.view":
                return 600;
            default:
                return 250;
        }
    }

    const colorScale = scaleLinear()
        .domain([-1, 0, 4])
        .range(['#ddd', yellow, '#e5e5e5'])
        .clamp(true);

    $: jumpOffset = calcJumpOffset($activeSections.pageKind);

    $: activeSectionIds = _.map($activeSections.sections, d => d.id);


    $: settingsCall = settingsStore.loadAll();
    $: settings = $settingsCall.data;

    $: logoOverlayText = _.find(settings, d => d.name === "ui.logo.overlay.text");
    $: logoOverlayColor = _.find(settings, d => d.name === "ui.logo.overlay.color");

    $: logo = $sidebarExpanded ? logoWithText : logoNoText;

</script>

<div class={$sidebarExpanded ? "sidebar-expanded" : "sidebar-collapsed" }>
    <div class="branding">
        <ViewLink state="main">
            <img height="32"
                 src={logo}
                 alt="Waltz logo">
            {#if $sidebarExpanded}
            <span class="overlay"
                  style={`color: ${logoOverlayColor?.value}`}>
                    {logoOverlayText?.value}
            </span>
            {/if}
        </ViewLink>
    </div>

    <ul class="list-unstyled">
        {#each $availableSections as section}
            <li class={_.includes(activeSectionIds, section.id)
                    ? "selected-sidenav"
                    : "sidenav"}>
                <button class="btn-skinny no-overflow"
                        title={section.description}
                        class:selected={_.includes(activeSections.sections, section)}
                        on:click={() => activateSection(section, jumpOffset)}>
                    <span style={`color: ${colorScale(_.indexOf(activeSectionIds, section.id))}`}>
                        <Icon size="lg"
                              name={section.icon}/>
                    </span>
                    <span class="section-name"
                          style={`opacity: ${$sidebarExpanded ? 1 : 0}`}>
                        {section.name}
                    </span>
                </button>
                {#if section.children}
                    <ul class="child-list list-unstyled">
                        {#each section.children as child}
                            <li class={_.includes(activeSectionIds, child.id)
                                    ? "selected-sidenav"
                                    : "sidenav"}>
                                <button class="btn-skinny no-overflow"
                                        title={child.description}
                                        on:click={() => activateSection(child, jumpOffset)}>
                                    <span style={`color: ${colorScale(_.indexOf(activeSectionIds, child.id))}`}>
                                        <Icon size="lg"
                                              name={child.icon}/>
                                    </span>
                                    <span class="section-name "
                                          style={`opacity: ${$sidebarExpanded
                                            ? 1
                                            : 0}`}>
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
          name={$sidebarExpanded
            ? 'angle-double-left'
            : 'angle-double-right'}>
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
        font-size: 50px;
        width: 100%;
        display: inline-block;
        text-align: right;
        transition: color ease-in-out 0.3s;
        transform: translateX(-4px);

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

    .branding {
        img {
            padding-left: 2em;
            position: relative;
            top: -6px;
        }

        padding-bottom: 2em;
    }

    .overlay {
        display: inline;
        position: absolute;
        left: 76px;
        top: 40px;
        font-weight: bold;
        font-style: italic;
        white-space: nowrap
    }
</style>