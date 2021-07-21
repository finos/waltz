<script>
    import {enumValueStore} from "../../../svelte-stores/enum-value-store";

    let enumCall = enumValueStore.load();

    $: ratings = _.chain($enumCall.data)
        .filter(d => d.type === "AuthoritativenessRating")
        .orderBy(["position", "name"])
        .value();

</script>

<ul style="display: inline"
    class="list-inline help-block">
    {#each ratings as rating}
    <li>
        <span class="indicator"
              style={`background-color: ${rating.iconColor}`}>
        </span>
        <span title={rating.description}>
            {rating.name}
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
