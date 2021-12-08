
<script>

    import EntityLink from "../EntityLink.svelte";
    import {personStore} from "../../../svelte-stores/person-store";
    import {orgUnitStore} from "../../../svelte-stores/org-unit-store";

    export let primaryEntityRef;

    $: personCall = personStore.getById(primaryEntityRef.id);
    $: person = $personCall.data;

    $: managerCall = person && personStore.getByEmployeeId(person?.managerEmployeeId);
    $: manager = $managerCall?.data;

    $: orgUnitCall = person?.organisationalUnitId && orgUnitStore.getById(person?.organisationalUnitId);
    $: orgUnit = $orgUnitCall?.data;

</script>

{#if person}
    <h4><EntityLink ref={person}/></h4>
    <slot name="post-title"/>
    <table class="table table-condensed small">
        <tbody>
            <tr>
                <td width="50%">Email</td>
                <td width="50%">{person.email}</td>
            </tr>
            <tr>
                <td width="50%">Org unit</td>
                <td width="50%">{orgUnit?.name || "Unknown"}</td>
            </tr>
            <tr>
                <td width="50%">Manager</td>
                <td width="50%">{manager?.name || "Unknown"}</td>
            </tr>
            <tr>
                <td width="50%">Title</td>
                <td width="50%">{person.title}</td>
            </tr>
        </tbody>
    </table>

    <slot name="post-header"/>

    <slot name="footer"/>
{/if}