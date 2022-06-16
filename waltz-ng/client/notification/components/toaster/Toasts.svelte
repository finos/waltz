<script>
    import Toast from "./Toast.svelte";
    import Toasts from "../../../svelte-stores/toast-store";
    import Markdown from "../../../common/svelte/Markdown.svelte";
</script>

{#if $Toasts}
    <section class="toasts">
        {#each $Toasts as toast (toast.id)}
            <Toast type={toast.type}
                   dismissible={toast.dismissible}
                   on:dismiss={() => Toasts.dismiss(toast.id)}>
                <Markdown text={toast.message}/>
            </Toast>
        {/each}
    </section>
{/if}

<style>
    .toasts {
        position: fixed;
        bottom: 0;
        right: 1%;
        display: flex;
        justify-content: flex-start;
        align-items: flex-end;
        flex-direction: column-reverse;
        z-index: 1000;
    }
</style>
