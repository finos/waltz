<script>
    import {settingsStore} from "../svelte-stores/settings-store";
    import logo from "../../images/branding/waltz_badge+text_horizontal_negative_bw_145px.png";
    import Icon from "../common/svelte/Icon.svelte";

    $: settingsCall = settingsStore.loadAll();
    $: settings = $settingsCall.data;

    $: logoOverlayText = _.find(settings, d => d.name === "ui.logo.overlay.text")
    $: logoOverlayColor = _.find(settings, d => d.name === "ui.logo.overlay.color")

    const navItems = [
        // { uiSref, icon, displayName, <role>, id }
        { uiSref: "main.org-unit.list", icon: "sitemap", displayName: "Org Units", id: "navbar-org-units" },
        { uiSref: "main.person", icon: "users", displayName: "People", id: "navbar-people" },
        { uiSref: "main.data-type.list", icon: "qrcode", displayName: "Data", id: "navbar-data-types" },
        { uiSref: "main.measurable-category.index", icon: "puzzle-piece", displayName: "Other Viewpoints", id: "navbar-measurables" },
    ];

</script>


<nav class="navbar navbar-default navbar-fixed-top">
    <!-- Brand and toggle get grouped for better mobile display -->
    <div class="container-fluid">
        <div class="navbar-header">
            <a class="navbar-brand"
               ui-sref="main.home">
                <img height="32"
                     style="position: relative; top: -6px;"
                     src={logo}
                     alt="Waltz logo">
            </a>
            <span class="overlay"
                  style={`color: ${logoOverlayColor?.value}`}>
                {logoOverlayText?.value}
            </span>
        </div>

    </div>

</nav>

<style>
    .overlay {
        display: inline;
        position: absolute;
        left: 72px;
        top: 30px;
        font-weight: bold;
        font-style: italic;
        white-space: nowrap
    }


</style>