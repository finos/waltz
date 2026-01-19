<script>
    import Markdown from "../../../../common/svelte/Markdown.svelte";

    export let node;
    export let url = null;
</script>

<div style="padding: 0.5em">
    <strong>{node?.title ?? node?.externalId}</strong>
    <hr>
    <dl>
        <dt>External Id</dt>
        <dd>{node?.externalId}</dd>
        <dt>Description</dt>
        <Markdown text={node?.description}></Markdown>
        <dt>Status</dt>
        <dd>{node?.status}</dd>
        {#if url !== null}
            <dt>URL</dt>
            <dd>
                <a href={`${url}/${node?.externalId}`} target="_blank" rel="noopener noreferrer">Link to ARC</a>
            </dd>
        {/if}
    </dl>
</div>
