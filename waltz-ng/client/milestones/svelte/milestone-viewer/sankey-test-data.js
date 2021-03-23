const categories = {
    b: { code: "b", color: '#7de951' },
    h: { code: "h", color: '#e35e5e' },
    s: { code: "s", color: '#f1c179' },
    n: { code: "n", color: '#9eeff5' },
    r: { code: "r", color: '#b1b1b1' }
};


const nodes = [
    {id: "b1", name: "Buy", category: categories.b, color: '#7de951', rank: 2},
    {id: "b2", name: "Buy", category: categories.b, color: '#7de951', rank: 2},
    {id: "h1", name: "Hold", category: categories.h, color: '#e35e5e', rank: 3},
    {id: "h2", name: "Hold", category: categories.h, color: '#e35e5e', rank: 3},
    {id: "s1", name: "Sell", category: categories.s, color: '#f1c179', rank: 4},
    {id: "s2", name: "Sell", category: categories.s, color: '#f1c179', rank: 4},
    {id: "n1", name: "New", category: categories.n, color: '#c3eaef', rank: 1},
    {id: "r2", name: "Retire", category: categories.r, color: '#e5acef', rank: 5}
];


export default function mkData() {
    return {
        nodes,
        links: [
            {
                source: "b1",
                target: "s2",
                value: Math.random() * 10
            }, {
                source: "s1",
                target: "r2",
                value: Math.random() * 10
            }, {
                source: "n1",
                target: "b2",
                value: Math.random() * 15
            }, {
                source: "b1",
                target: "h2",
                value: Math.random() * 10
            }, {
                source: "b1",
                target: "b2",
                value: Math.random() * 10
            }, {
                source: "s1",
                target: "s2",
                value: Math.random() * 5
            }, {
                source: "h1",
                target: "h2",
                value: Math.random() * 20
            }, {
                source: "h1",
                target: "r2",
                value: Math.random() * 7
            }
        ]
    };
}