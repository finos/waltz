<script>


    import {diagramService} from "../entity-diagram-store";
    import {onMount} from "svelte";

    const {updateOverlayParameters} = diagramService;

    onMount(() => {
        updateOverlayParameters({});
    })

</script>

<div>
    There is nothing to configure for the aggregate entities overlay
</div>