<script>

    import WidgetSelector from "../aggregate-overlay-diagram/WidgetSelector.svelte";
    import CalloutList from "../aggregate-overlay-diagram/CalloutList.svelte";

    export let primaryEntityRef;
    export let handleWidgetChange;

</script>


<div>
    <WidgetSelector on:change={handleWidgetChange}
                    {primaryEntityRef}/>
</div>
<div>
    <CalloutList/>
</div>