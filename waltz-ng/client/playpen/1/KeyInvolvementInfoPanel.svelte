<script>

    import {involvementViewStore} from "../../svelte-stores/involvement-view-store";

    export let primaryEntityRef;

    $: involvementsCall2 = involvementViewStore.findKeyInvolvementsForEntity(primaryEntityRef);
    $: involvements2 = $involvementsCall2.data;

    $: tableData =  _
        .chain(involvements2)
        .groupBy(d => d.involvementKind.id)
        .map((v, k) => Object.assign(
            {},
            {
                involvementKind: _.first(v).involvementKind,
                people: _.map(v, d => d.person)
            }))
        .value()

    function mkNamesString(people){
        const names = _.map(people, p => p.name);
        return _.join(names, ", ");
    }

</script>

<table class="table table-condensed small">
    <thead>
        <th width="50%">Key People:</th>
        <th width="50%"></th>
    </thead>
    <tbody>
    {#each tableData as keyPerson}
        <tr>
            <td>{keyPerson.involvementKind.name}</td>
            <td>{mkNamesString(keyPerson.people)}</td>
        </tr>
    {/each}
    </tbody>
</table>