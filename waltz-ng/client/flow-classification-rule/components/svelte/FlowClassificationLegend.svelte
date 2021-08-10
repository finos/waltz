<script>
    import {flowClassificationStore} from "../../../svelte-stores/flow-classification-store";

    let flowClassificationCall = flowClassificationStore.findAll();

    $: classifications = _.orderBy($flowClassificationCall.data, ["position", "name"]);

</script>

<ul style="display: inline"
    class="list-inline help-block">
    {#each classifications as classification}
    <li>
        <span class="indicator"
              style={`background-color: ${classification.color}`}>
        </span>
        <span title={classification.description}>
            {classification.name}
        </span>
    </li>
    {/each}
</ul>

<style>
    .indicator {
        display: inline-block;
        border: 1px solid #ccc;
        height: 0.9em;
        width: 1em;
    }
</style>
