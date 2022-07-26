<script>

    import Markdown from "../../../common/svelte/Markdown.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    export let definition;

</script>


<h4>
    {definition.name}
    <span class="text-muted small">({definition.externalId})</span>
</h4>
<Markdown class="force-wrap" text={definition.description}/>

{#if definition.isReadOnly}
    <div class="help-block small">
        <Icon name="lock"/>
        Assessments relating to this definition are read-only and cannot be edited.
    </div>
{/if}