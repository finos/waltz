<script>
    import {settingsStore} from "../svelte-stores/settings-store";
    import logo from "../../images/branding/waltz_badge+text_horizontal_negative_bw_145px.png";

    $: settingsCall = settingsStore.loadAll();
    $: settings = $settingsCall.data;

    $: logoOverlayText = _.find(settings, d => d.name === "ui.logo.overlay.text")
    $: logoOverlayColor = _.find(settings, d => d.name === "ui.logo.overlay.color")

</script>


<nav class="navbar navbar-default navbar-fixed-top">
    <!-- Brand and toggle get grouped for better mobile display -->
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