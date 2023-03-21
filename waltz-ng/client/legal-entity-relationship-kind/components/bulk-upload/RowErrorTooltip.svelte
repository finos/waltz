<script>


    export let relationshipErrors = [];

</script>

<strong>This relationship has the following errors:</strong>
<ul>
    {#each relationshipErrors as error}
        <li>{error.errorMessage}</li>
    {/each}
</ul>