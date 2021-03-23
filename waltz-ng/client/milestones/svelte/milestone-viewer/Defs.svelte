<script>
    import {backgroundColors, foregroundColors} from "./stores/decorators";
</script>
<defs>
    {#each $backgroundColors.domain() as color}
        <linearGradient spreadMethod="pad" id="gradient-bg-{color}" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" style="stop-color:{$backgroundColors(color)};stop-opacity:1;" />
            <stop offset="100%" style="stop-color:rgb(255, 255, 255);stop-opacity:1;" />
        </linearGradient>
    {/each}
    {#each $foregroundColors.domain() as color}
        <linearGradient spreadMethod="pad" id="gradient-fg-{color}" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" style="stop-color:{$foregroundColors(color)};stop-opacity:1;" />
            <stop offset="100%" style="stop-color:rgb(255, 255, 255);stop-opacity:1;" />
        </linearGradient>
    {/each}
</defs>