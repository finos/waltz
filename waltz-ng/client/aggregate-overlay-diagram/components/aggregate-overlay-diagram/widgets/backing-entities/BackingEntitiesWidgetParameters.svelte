<script>
    import {getContext} from "svelte";

    const widgetParameters = getContext("widgetParameters");
    const selectedOverlay = getContext("selectedOverlay");

    $widgetParameters = {};
    $selectedOverlay = null;
</script>

<div class="help-block">
    The backing widget overlay has no configurable parameters
</div>