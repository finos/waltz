<script>

    import EntityLink from "../../../../common/svelte/EntityLink.svelte";

    export let data;
    // export let cellData;

</script>

<div style="align-content: center; text-align: center">
    <EntityLink ref={data.entityReference}
                showIcon={false}/>
</div>
