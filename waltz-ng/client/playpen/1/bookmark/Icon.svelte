<script>
    export let name = "circle-o";
    export let size;          // lg | 2x | 3x | 4x | 5x
    export let flip;          // 90 | 180 | 270
    export let rotate;       // horizontal | vertical
    export let stack;
    export let fixedWidth;
    export let inverse;
    export let spin;

    let classNames = [];

    $: classNames = [
        "fa",
        `fa-${name}`,
        flip ? `fa-flip-${flip}` : "",
        rotate ? `fa-rotate-${rotate}` : "",
        size ? `fa-${size}` : "",
        stack ? `fa-stack-${stack}` : "",
        fixedWidth ? "fa-fw" : "",
        inverse ? "fa-inverse" : "",
        spin ? "fa-spin" : ""
    ].join(" ");

</script>

<style>
    .icon {
        font-size: smaller;
        opacity: 0.7;
    }
</style>

<span class="icon">
    <i class={classNames}/>
</span>
