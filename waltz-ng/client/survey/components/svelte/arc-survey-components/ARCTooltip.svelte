<script>
    export let node;
    export let url = null;
</script>

<div style="padding: 0.5em">
    <strong>{node?.externalId}</strong>
    <hr>
    <dl>
        <dt>Summary</dt>
        <dd>{node?.title}</dd>
        <dt>Assignee</dt>
        <dd>{node?.description}</dd>
        <dt>Status</dt>
        <dd>{node?.status}</dd>
        <dt>Resolution</dt>
        <dd>{node?.milestoneRag}</dd>
        {#if url !== null}
            <dt>URL</dt>
            <dd>
                <a href={`${url}/${node?.externalId}`} target="_blank" rel="noopener noreferrer">Link to ARC</a>
            </dd>
        {/if}
    </dl>
</div>
