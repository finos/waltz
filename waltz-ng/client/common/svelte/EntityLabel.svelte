<script>
    import EntityIcon from "./EntityIcon.svelte";

    export let ref;

    const nameMap = {
        SERVER: "hostname",
        DATABASE: "databaseName"
    }

    $: name = _.get(ref, [nameMap[ref.kind] || "name"], "unknown");

</script>
<EntityIcon kind={ref.kind}/>
{name}