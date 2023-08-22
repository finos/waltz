<script>


    import {onMount} from "svelte";
    import {diagramService} from "../entity-diagram-store";

    const {updateOverlayParameters} = diagramService;

    onMount(() => {
        updateOverlayParameters({});
    })


</script>

<div>
    There is nothing to configure for this overlay
</div>