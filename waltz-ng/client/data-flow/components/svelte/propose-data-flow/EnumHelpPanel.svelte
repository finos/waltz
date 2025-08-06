<script>

    export let options = [];

</script>


<table class="table table-condensed table-striped">
    <colgroup>
        <col width="30%">
        <col width="70%">
    </colgroup>

    <thead>
    <tr>
        <th>
            Transport Kind
        </th>
        <th>
            Description
        </th>
    </tr>
    </thead>
    <tbody>
    {#each options as option}
        <tr>
            <td>{option.name}</td>
            <td>{option.description}</td>
        </tr>
    {/each}
    </tbody>
</table>