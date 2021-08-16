<script>
    import {createEventDispatcher} from "svelte";
    import {fade} from "svelte/transition";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {amber, blue, green, red} from "../../../common/colors";

    const NOTIFICATION_TYPES = {
        "success": {iconName: "check", color: green},
        "warning": {iconName: "exclamation", color: amber},
        "error": {iconName: "exclamation", color: red},
        "info": {iconName: "info", color: blue},
    }

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

<style type="text/scss">

  @import "./style/variables";

    article {
        color: white;
        padding: 0.75rem 1.5rem;
        border-radius: 0.2rem;
        display: flex;
        align-items: center;
        margin: 0 auto 0.5rem auto;
        width: 40rem;
    }
    .text {
        margin-left: 1rem;
    }
    button {
        color: white;
        background: transparent;
        border: 0 none;
        padding: 0;
        margin: 0 0 0 auto;
        line-height: 1;
        font-size: 1rem;
    }
</style>
