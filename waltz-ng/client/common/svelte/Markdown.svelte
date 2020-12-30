<script>
    import showdown from "showdown";

    export let text = "";

    const converter = new showdown.Converter();
    converter.setFlavor("github")

    $: html = converter.makeHtml(text);
</script>

{@html html}