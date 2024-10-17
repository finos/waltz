<script>
  export let errors;
</script>


{#if errors && errors.length}
    {#each errors as error}
      <div>{error}</div>
    {/each}
{/if}

<style>
	div {
		color: red;
    font-size: 12px;
	}
</style>