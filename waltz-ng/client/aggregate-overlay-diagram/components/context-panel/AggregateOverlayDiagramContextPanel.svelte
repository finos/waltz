<script>

    import WidgetSelector from "../aggregate-overlay-diagram/WidgetSelector.svelte";
    import CalloutList from "../aggregate-overlay-diagram/CalloutList.svelte";

    export let primaryEntityRef;
    export let handleWidgetChange;

</script>

<div>
    <CalloutList/>
</div>