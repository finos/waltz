<script>
    export let name = "circle-o";
    export let size;          // lg | 2x | 3x | 4x | 5x
    export let flip;          // 90 | 180 | 270
    export let rotate;       // horizontal | vertical
    export let stack;
    export let fixedWidth = true;
    export let inverse;
    export let spin;
    export let pullLeft = false;
    export let pullRight = false;

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
        spin ? "fa-spin" : "",
        pullLeft ? "fa-pull-left" : "",
        pullRight ? "fa-pull-right" : ""
    ].join(" ");

</script>

<style>
    .icon {
        font-size: smaller;
    }
</style>

<span class="icon"
      data-ux={name}>
    <i class={classNames}/>
</span>
