<script>
    import EntityIcon from "./EntityIcon.svelte";

    export let ref;

</script>
<EntityIcon kind={ref.kind}/>
{ref.name}