<script>

    import tippy from "tippy.js";
    import 'tippy.js/dist/tippy.css';
    import 'tippy.js/themes/light-border.css';

    let elem;
    let contentElem;
    let open = false;

    export let content;
    export let props;
    export let trigger = "mouseenter click";
    export let placement = "top";
    export let delay = [300, 100];


    $: {

        if (elem && content) {
            tippy(elem, {
                content: "loading",
                arrow: true,
                interactive: true,
                appendTo: document.body,
                trigger,
                theme: "light-border",
                placement,
                delay,
                onShow(instance) {
                    open = true;
                    setTimeout(() => instance.setContent(contentElem), 100);
                }
            });
        }
    }


</script>

<span bind:this={elem}>
    <slot name="target"></slot>
</span>

<div style="display: none">
    <div bind:this={contentElem}>
        {#if open}
            <svelte:component this={content} {...props}/>
        {/if}
    </div>
</div>
