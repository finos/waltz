<script>
    import {createEventDispatcher} from "svelte";
    import {fade} from "svelte/transition";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {amberBg, blueBg, greenBg, redBg} from "../../../common/colors";

    const NOTIFICATION_TYPES = {
        "success": {iconName: "check", color: greenBg},
        "warning": {iconName: "exclamation", color: amberBg},
        "error": {iconName: "exclamation", color: redBg},
        "info": {iconName: "info", color: blueBg},
    };

    const dispatch = createEventDispatcher();

    export let type = "info";
    export let dismissible = true;

    $: notificationType = NOTIFICATION_TYPES[type] || NOTIFICATION_TYPES.info;
</script>

<article style={`background: ${notificationType.color}`}
         transition:fade>

    <Icon name={notificationType.iconName} width="1.1em" />

    <div class="text">
        <slot />
    </div>

    {#if dismissible}
        <button class="close" on:click={() => dispatch("dismiss")}>
            <Icon name="times" width="0.8em" />
        </button>
    {/if}
</article>

<style>
    article {
        padding: 0.75rem 1.5rem;
        border-radius: 0.2rem;
        display: flex;
        align-items: center;
        margin: 0 auto 0.5rem auto;
        width: 40rem;
        border: 1px solid #999999;
    }
    .text {
        margin-left: 1rem;
        margin-top: 10px;
    }
    button {
        background: transparent;
        border: 0 none;
        padding: 0;
        margin: 0 0 0 auto;
        line-height: 1;
        font-size: 2rem;
    }
</style>
