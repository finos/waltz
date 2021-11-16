<script>

    import EntityLink from "../../common/svelte/EntityLink.svelte";

    export let response;

</script>

{#if response}
    <EntityLink ref={response?.entityResponse}/>
{/if}