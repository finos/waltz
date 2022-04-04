<script>

    export let data;
    export let color = "red";


</script>


<circle on:mouseover={() => console.log({data})} r="10" fill={data.value === 0 ? "#ccc" : color}></circle>