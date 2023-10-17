<script>
    import _ from "lodash";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";

    export let response;

    $: items = _.get(response, ["entityListResponse"], []);
</script>

{#if !_.isEmpty(items)}
<table class="table table-condensed small">
    <thead>
    <tr>
        <th>Identifier</th>
        <th>Legal Entity</th>
    </tr>
    </thead>
    <tbody>
    {#each items as item}
    <tr>
        <td>
            <span>{item.externalId}</span>
        </td>
        <td>
            <EntityLink ref={item} />
        </td>
    </tr>
    {/each}
    </tbody>
</table>
{/if}