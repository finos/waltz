<script>
    export let resolvedRow;
</script>


{#if resolvedRow.errorMessage}
    <div>
        {resolvedRow.errorMessage}
    </div>
{/if}