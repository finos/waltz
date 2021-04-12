<script>
    import Icon from "./Icon.svelte";
</script>

<h3 class="text-muted">
    <Icon name="circle-o-notch"
          spin="true"/>
    <slot>
        Loading ...
    </slot>
</h3>
