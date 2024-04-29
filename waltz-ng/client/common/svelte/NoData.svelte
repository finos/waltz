<script>
    export let type = "dflt"; // dflt | info | warning
</script>

<div class={type}>
    <slot></slot>
</div>

<style>
    .dflt {
        background: #fffdfa;
        background: linear-gradient(90deg, #fffdfa 0%, rgba(255,255,255,1) 100%);
        color: #7B7D7D;
        padding: 15px;
        margin-bottom: 6px;
        border-radius: 3px;
        box-shadow: 0 0 2px 0 hsla(0, 0%, 0%, 0.2);
        border-left: 4px solid rgba(222, 115, 119, 0.99);
    }

    .info {
        background: #fafcff;
        background: linear-gradient(90deg, #fafdff 0%, rgb(226, 241, 252) 100%);
        color: #7b7c7d;
        padding: 15px;
        margin-bottom: 6px;
        border-radius: 3px;
        box-shadow: 0 0 2px 0 hsla(0, 0%, 0%, 0.2);
        border-left: 4px solid rgb(115, 163, 222);
    }

    .warning {
        background: #fffcfa;
        background: linear-gradient(90deg, #fffdfa 0%, rgb(252, 246, 226) 100%);
        color: #7d7c7b;
        padding: 15px;
        margin-bottom: 6px;
        border-radius: 3px;
        box-shadow: 0 0 2px 0 hsla(0, 0%, 0%, 0.2);
        border-left: 4px solid rgb(245, 196, 123);
    }

    .error {
        background: #fffcfa;
        background: linear-gradient(90deg, #fffdfa 0%, rgb(248, 229, 229) 100%);
        color: #7d7c7b;
        padding: 15px;
        margin-bottom: 6px;
        border-radius: 3px;
        box-shadow: 0 0 2px 0 hsla(0, 0%, 0%, 0.2);
        border-left: 4px solid rgb(245, 133, 123);
    }
</style>