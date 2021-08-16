<script>
    import Toast from "./Toast.svelte";
    import Toasts from "./toast-store";

</script>

{#if $Toasts}
    <section>
        {#each $Toasts as toast (toast.id)}
            <Toast type={toast.type}
                    dismissible={toast.dismissible}
                    on:dismiss={() => Toasts.dismiss(toast.id)}>
                {@html toast.message}
            </Toast>
        {/each}
    </section>
{/if}

<style>
    section {
        position: fixed;
        bottom: 0;
        right: 1%;
        top: auto;
        height: 100%;
        display: flex;
        justify-content: flex-start;
        align-items: flex-end;
        flex-direction: column-reverse;
        z-index: 1000;
    }
</style>
