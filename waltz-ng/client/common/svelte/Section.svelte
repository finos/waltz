<script>

    import Icon from "./Icon.svelte";

    export let icon = "fw";
    export let name;
    export let small;

</script>

<div>
    <div class="waltz-section">

        <div class="waltz-section-header">
            <div class="waltz-visibility-parent waltz-section-header-title">
                <Icon name={icon}/>
                <span>{name}</span>

                {#if small}
                    <small>
                        <span>{small}</span>
                    </small>
                {/if}
            </div>
        </div>

        <div class="container-fluid waltz-section-body animate-if">
            <slot></slot>
        </div>
    </div>
</div>
