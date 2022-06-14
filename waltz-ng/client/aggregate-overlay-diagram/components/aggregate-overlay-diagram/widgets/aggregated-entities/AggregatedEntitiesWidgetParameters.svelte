<script>
    import {getContext} from "svelte";

    const widgetParameters = getContext("widgetParameters");
    const selectedOverlay = getContext("selectedOverlay");

    $widgetParameters = {};
    $selectedOverlay = null;
</script>

