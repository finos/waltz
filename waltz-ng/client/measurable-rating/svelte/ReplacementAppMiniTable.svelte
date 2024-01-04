<script>
    import _ from "lodash";
    import EntityLabel from "../../common/svelte/EntityLabel.svelte";

    export let replacementApps = [];

</script>

<table class="table table-condensed small">
    <thead>
        <tr>
            <th>Application</th>
            <th>Commission Date</th>
        </tr>
    </thead>
    <tbody>
    {#each _.orderBy(replacementApps, d => d.entityReference.name) as app}
        <tr>
            <td>
                <EntityLabel ref={app.entityReference}
                             showIcon={false}>
                </EntityLabel>
            </td>
            <td>
                {app.plannedCommissionDate}
            </td>
        </tr>
    {/each}
    </tbody>
</table>


<style>

    table {
        width: 100%;
    }

    table td {
        padding-bottom: 0.2em;
        padding-right: 0.6em;
        border-bottom: 1px solid #eee;
        vertical-align: top;
    }
</style>