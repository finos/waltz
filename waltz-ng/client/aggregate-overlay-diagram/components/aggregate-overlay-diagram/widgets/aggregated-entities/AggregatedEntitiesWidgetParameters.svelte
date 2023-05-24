<script>
    import {getContext} from "svelte";

    const widgetParameters = getContext("widgetParameters");
    const selectedOverlay = getContext("selectedOverlay");

    $widgetParameters = {};
    $selectedOverlay = null;
</script>


<div class="help-block">
    The aggregated entities overlay has no configurable parameters
</div>
