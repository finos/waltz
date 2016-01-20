import { randomPick } from '../common';

const rag = () => randomPick(['R', 'A', 'G']);

export default {
    't': '0',
    'children': [
        {
            't': '00',
            'value': 5,
            rating: rag()
        },
        {
            't': '01',
            'children': [
                {
                    't': '010',
                    'value': 1,
                    rating: rag()
                },
                {
                    't': '011',
                    'value': 1,
                    rating: rag()
                },
                {
                    't': '012',
                    'value': 1,
                    rating: rag()
                },
                {
                    't': '013',
                    'value': 1,
                    rating: rag()
                }
            ],
            rating: rag()
        },
        {
            't': '02',
            'children': [
                {
                    't': '020',
                    'value': 1,
                    rating: rag()
                },
                {
                    't': '021',
                    'value': 1,
                    rating: rag()
                },
                {
                    't': '022',
                    'value': 1,
                    rating: rag()
                },
                {
                    't': '023',
                    'value': 1,
                    rating: rag()
                },
                {
                    't': '024',
                    'value': 1,
                    rating: rag()
                },
                {
                    't': '025',
                    'value': 1,
                    rating: rag()
                }
            ],
            rating: rag()
        },
        {
            't': '03',
            'children': [
                {
                    't': '030',
                    'value': 1,
                    rating: rag()
                },
                {
                    't': '031',
                    'value': 1,
                    rating: rag()
                },
                {
                    't': '032',
                    'value': 1,
                    rating: rag()
                },
                {
                    't': '033',
                    'value': 1,
                    rating: rag()
                }

            ],
            rating: rag()
        },
        {
            't': '04',
            'value': 5,
            rating: rag()
        }
    ],
    rating: rag(),
    capability: { name: 'Trading', description: 'About trading' }
};
