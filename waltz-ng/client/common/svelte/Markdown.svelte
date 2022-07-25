<script context="module">
    import showdown from "showdown";

    showdown.extension("bs-tables", () => {
        return [{
            type: "output",
            regex: /<table>/g,
            replace: "<table class='table'>",
        }]
    });

    const converter = new showdown.Converter({extensions: ["bs-tables"]});
    converter.setFlavor("github");
</script>


<script>
    import _ from "lodash";

    export let text = "";
    export let context = {};
    export let inline = false;

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

<style type="text/scss">

    :global(.inline-markdown > p) {
        display: inline;
    }


</style>