import {BaseType, select} from 'd3-selection';

interface Datum {
    name: string,
    age: number
};


class Chart {
    private _group: d3.Selection<any, Datum, any, any>;

    private ds: Datum[] = [];

    constructor(container: any) {
        this.init(container);
}

    private init(container: any) {
        this._group = select(container)
            .append('ul')
            .selectAll('li')
            .data(this.ds)
            .enter()
            .append("li")
            .text(d => d.name);
    }

}

export { Chart };
