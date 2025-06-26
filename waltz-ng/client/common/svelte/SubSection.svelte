<script>

    export let showBorder = true;

</script>

<div class="sub-section"
     class:show-border={showBorder}>
    <div class="name">
        <slot name="header"></slot>
    </div>

    <div class="content">
        <slot name="content"></slot>
    </div>

    <div class="controls">
        <slot name="controls"></slot>
    </div>

    <div class="content">
        <slot name="changes"></slot>
    </div>
</div>

<style type="text/scss">

    .sub-section {
        margin-right: 0;
        margin-left: 0;
        margin-bottom: 4px;

        &.show-border {
            box-shadow: 0 1px 2px 0 hsla(0, 0%, 0%, 0.2);
            border-radius: 1px;
            border-top: 1px solid #f9f9f4;
            transition: box-shadow ease-in-out 0.3s;

            &:hover {
                box-shadow: 0 2px 4px 0 hsla(0, 0%, 0%, 0.2);
            }
        }

        .name {
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

        &:hover .name {
            font-weight: bolder;
        }
    }

    .content {
        margin: 6px 16px;
        padding-bottom: 0.5em;
    }

    .controls {
        padding-bottom: 2px;
        font-size: smaller;
        height: 1.8em;
        margin: 0;
        display: block;
        width: 100%;
        border-top: 1px solid #f9f9f4;
        background: linear-gradient(90deg, rgb(254, 254, 254) 50%, #f9f9f4 100%);
        transition: font-weight ease-in-out 300ms;
    }

</style>