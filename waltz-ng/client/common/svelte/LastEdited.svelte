<script>
    import DateTime from "./DateTime.svelte";
    export let entity;
</script>

<span>
    {entity.lastUpdatedBy},
    <DateTime relative={true}
              formatStr="yyyy-MM-DD"
              dateTime={entity.lastUpdatedAt}/>
</span>
