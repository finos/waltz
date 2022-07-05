<script>
    import Icon from "./Icon.svelte";
    import html2canvas from "html2canvas";

    export let name = "Download Image";
    export let styling = "button";  // button | link
    export let filename = "image.png";
    export let element = null;
    export let selector = null; // optional css selector

    let busy = false;
    let headerElem = null;
    let footerElem = null;

    function calcClasses(styling = "button") {
        switch (styling) {
            case "link":
                return ["btn-link"];
            default:
                return ["btn", "btn-xs", "btn-default"];
        }
    }

    function downloadImage() {
        const elem = element || document.querySelector(selector);

        if (elem) {
            busy = true;
            //Using a timeout so browser has chance to display the progress icon
            setTimeout(() => {
                    if ($$slots.header) {
                        elem.prepend(headerElem);
                    }
                    if ($$slots.footer) {
                        elem.append(footerElem);
                    }

                    return html2canvas(elem, {allowTaint: true})
                        .then(canvas => {
                            document.body.appendChild(canvas);
                            return canvas;
                        })
                        .then(canvas => {
                            const image = canvas
                                .toDataURL('image/png')
                                .replace('image/png', 'image/octet-stream');
                            const a = document.createElement('a');
                            a.setAttribute('download', filename);
                            a.setAttribute('href', image);
                            a.click();
                            canvas.remove();
                        })
                        .finally(() => {
                            if (elem.contains(headerElem)) {
                                elem.removeChild(headerElem);
                            }
                            if (elem.contains(footerElem)) {
                                elem.removeChild(footerElem);
                            }
                            busy = false;
                        });

                },
                0);
        }
    }

    $: classes = calcClasses(styling);

</script>


<!-- format specified, eg: SVG -->
<button class={classes}
        disabled={!element && !selector}
        title={!element && !selector
            ? "No image to download"
            : "Download the image in png format"}
        on:click={downloadImage}>
    <Icon name={busy ? "refresh" : "cloud-download"}
          fixedWidth={true}
          spin={busy}/>
    {name}
</button>

<div style="display: none">
    <div bind:this={headerElem}>
        <slot name="header"/>
    </div>

    <div bind:this={footerElem}>
        <slot name="footer"/>
    </div>
</div>



<style>

</style>