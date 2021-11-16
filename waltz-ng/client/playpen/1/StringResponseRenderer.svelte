<script>

    import Markdown from "../../common/svelte/Markdown.svelte";

    export let response;

</script>

<Markdown text={response?.stringResponse}/>