<script>

    import _ from "lodash";
    export let response;

    $: items = _.get(response, ["listResponse"], []);

</script>


<ul>
    {#each items as item}
        <li>{item}</li>
    {/each}
</ul>


<style>

    ul {
        padding-left: 1em;
    }

</style>