<script>
    import Markdown from "../../../../common/svelte/Markdown.svelte";
    import _ from "lodash";

    export let response;

</script>

{#if !_.isEmpty(response?.stringResponse)}
    <Markdown text={response?.stringResponse}/>
{:else}
    <span class="text-muted">-</span>
{/if}