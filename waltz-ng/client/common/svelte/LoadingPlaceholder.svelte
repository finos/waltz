<script>
    import Icon from "./Icon.svelte";
</script>

<h3 class="text-muted">
    <Icon name="sync"
          spin={true}/>
    <slot>
        Loading ...
    </slot>
</h3>
