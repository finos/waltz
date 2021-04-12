<script>
    import DataTypeTreeSelector from "../../common/svelte/DataTypeTreeSelector.svelte";
</script>



<DataTypeTreeSelector on:select={(evt) => console.log(evt.detail)}></DataTypeTreeSelector>