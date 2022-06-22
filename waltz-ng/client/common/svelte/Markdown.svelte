<script>
    import showdown from "showdown";
    import _ from "lodash";

    export let text = "";
    export let context = {};
    export let inline = false;

    const converter = new showdown.Converter();
    converter.setFlavor("github");

    function mkHtml(markdown, ctx) {
        try {
            const markdownText = _.isEmpty(ctx)
                ? markdown
                : _.template(markdown, { variable: "ctx"})(ctx);  // creates template function then invokes with `ctx`

            return converter.makeHtml(markdownText);
        } catch (e) {
            console.log("Failed to render markdown with context", { context, markdown, e })
        }
    }

    $: html = mkHtml(text, context);
</script>

<span class:inline-markdown={inline}>
    {@html html}
</span>

<style>

    :global(.inline-markdown > p) {
        display: inline;
    }

</style>