<script>

    import {involvementViewStore} from "../../../svelte-stores/involvement-view-store";
    import EntityLink from "../EntityLink.svelte";
    import _ from "lodash"

    export let primaryEntityRef;

    $: involvementsCall = involvementViewStore.findKeyInvolvementsForEntity(primaryEntityRef);
    $: involvements = $involvementsCall.data;

    $: tableData = _
        .chain(involvements)
        .groupBy(d => d.involvementKind.id)
        .map((v, k) => Object.assign(
            {},
            {
                involvementKind: _.first(v).involvementKind,
                people: _.map(v, d => d.person)
            }))
        .value()

</script>

{#if !_.isEmpty(tableData)}
    <table class="table table-condensed small">
        <thead>
            <th width="50%">Key People:</th>
            <th width="50%"></th>
        </thead>
        <tbody>
        {#each tableData as keyPerson}
            <tr>
                <td>{keyPerson.involvementKind.name}</td>
                <td>
                    {#each keyPerson.people as person}
                        <span>
                            {#if _.first(keyPerson.people).id !== person.id};&nbsp;{/if}
                            <EntityLink ref={person}/>
                        </span>
                    {/each}
                </td>
            </tr>
        {/each}
        </tbody>
    </table>
{/if}