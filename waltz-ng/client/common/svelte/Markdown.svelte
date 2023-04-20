<script>
    import _ from "lodash";
    import {markdownToHtml} from "../markdown-utils";

    export let text = "";
    export let context = {};
    export let inline = false;

    function mkHtml(markdown, ctx) {
        try {
            const markdownText = _.isEmpty(ctx)
                ? markdown
                : _.template(markdown, { variable: "ctx"})(ctx);  // creates template function then invokes with `ctx`

            return markdownToHtml(markdownText);
        } catch (e) {
            console.log("Failed to render markdown with context", { context, markdown, e })
        }
    }

    $: convertedHtml = mkHtml(text, context);
</script>


<span class:inline-markdown={inline}>
    {@html convertedHtml}
</span>


<style>
    :global(.inline-markdown > p) {
        display: inline;
    }
</style>