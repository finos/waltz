<script>
    import {symbolsByName} from "../flow-diagram-utils";

    export let overlay; // {symbol, stroke, fill}
    export let width = 10;
    export let height = 10;
</script>

<svg title="{overlay.symbol}/{overlay.fill}"
     {width}
     {height}
     viewBox="0 0 10 10">
    <path fill={overlay.fill}
          stroke={overlay.stroke}
          transform="translate(5 5)"
          d={symbolsByName[overlay.symbol]()}>
    </path>
</svg>