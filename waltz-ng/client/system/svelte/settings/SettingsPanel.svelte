<script>

    import {settingsStore} from "../../../svelte-stores/settings-store";

    $: settingsCall = settingsStore.loadAll();
    $: settings = $settingsCall.data;


    $: console.log({settings});

</script>

<h4>Hi</h4>
<table class="table table-condensed">
    <thead>
    <tr>
        <th>Key</th>
        <th>Value</th>
        <th>Description</th>
        <th>Restricted?</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>1</td>
        <td>2</td>
        <td>3</td>
    </tr>
    </tbody>
</table>