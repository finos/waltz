<script>

    export let showBorder = true;

</script>

<div class="waltz-sub-section"
     class:show-border={showBorder}>
    <div class="wss-name">
        <slot name="header"></slot>
    </div>

    <div class="waltz-sub-section-content">
        <slot name="content">
        </slot>
    </div>
</div>

<style type="text/scss">

    .waltz-sub-section {
        margin-right: 0;
        margin-left: 0;
        margin-bottom: 4px;
        padding-bottom: 0.5em;

        &.show-border {
            box-shadow: 0 1px 2px 0 hsla(0, 0%, 0%, 0.2);
            border-radius: 1px;
            border-top: 1px solid #f9f9f4;
            transition: box-shadow ease-in-out 0.3s;

            &:hover {
                box-shadow: 0 2px 4px 0 hsla(0, 0%, 0%, 0.2);

            }
        }

        .wss-name {
            padding-top: 2px;
            padding-left: 8px;
            padding-bottom: 2px;
            font-size: 12px;
            font-weight: 700;
            color: #959595;
            text-transform: uppercase;
            letter-spacing: 1px;
            border-bottom: 1px solid #f9f9f4;
            background: linear-gradient(270deg, rgb(254, 254, 254) 50%, #f9f9f4 100%);
            transition: font-weight ease-in 0.3s;
        }

        &:hover .wss-name {
            font-weight: bolder;
        }
    }

    .waltz-sub-section-content {
        margin: 6px 16px;
    }

</style>