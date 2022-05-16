<script>
    import {getContext} from "svelte";
    import Callout from "./Callout.svelte";

    let callouts = getContext("callouts");
</script>


{#each $callouts as c, idx}
    <div class="overlay-cell {c.cellExternalId}"
         data-cell-id={c.cellExternalId}>
        <Callout callout={c}
                 label={idx + 1}/>
    </div>
{/each}
