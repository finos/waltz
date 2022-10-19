<script>


    import Markdown from "../../../common/svelte/Markdown.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    export let primaryEntityRef;
    export let grid;
    export let filters;


    $: console.log({primaryEntityRef, grid, filters})

    $: noteContent = "| Grid Name | Grid Identifier | Vantage Point Kind | Vantage Point Id |\n" +
        "| --- | --- | --- | --- |\n" +
        `| \`${grid.definition.name}\` | \`${grid.definition.externalId}\` | \`${primaryEntityRef.kind}\` | \`${primaryEntityRef.id}\` |\n` +
        "\n" +
        "\n" +
        "| Filter Column | Filter Operator | Value/s |\n" +
        "| --- | --- | --- |\n";


    $: console.log({noteContent});


</script>

<div style="padding: 0.5em 0"
     class="pull-right">
    <button class="btn btn-default">
        <Icon name="clone"/>
        Copy Note Content
    </button>
</div>

<div style="padding: 0.5em 0">
    <textarea class="form-control"
              id="content"
              placeholder="Note content"
              disabled={true}
              rows="10"
              bind:value={noteContent}/>
    <div class="help-block">
        <Icon name="info-circle"/>
        Content to copy and paste into the 'Notes' section of your application group.
    </div>
</div>


<div style="padding: 0.5em 0">
    <Markdown class="force-wrap" text={noteContent}/>
    <div class="help-block">
        <Icon name="info-circle"/>
        Preview of the formatted note once saved
    </div>
</div>
