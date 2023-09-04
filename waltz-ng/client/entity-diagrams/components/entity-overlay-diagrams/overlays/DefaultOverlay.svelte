<script>

    import EntityLink from "../../../../common/svelte/EntityLink.svelte";

    export let data;

</script>

{#if data}
    <div style="align-content: center; text-align: center">
        <EntityLink ref={data.entityReference}
                    showIcon={false}/>
    </div>
{/if}
