const pactum = require('pactum');
var itParam = require('mocha-param');
const {any} = require("pactum-matchers");

const SETTINGS = ['currency.primary', 'ui.transaction.closedialog', 'ui.language'];

it('All settings are present in the settings list', async () => {
    await pactum.spec('read')
        .get('/setting')
        .expectJsonLike('data[*].id', SETTINGS);
});

itParam("Loading setting ${value}", SETTINGS, async (params) => {
    await pactum.spec('read')
        .get('/setting/{id}')
        .withPathParams('id', params)
        .expectJsonMatch('data', {
            'type': 'setting',
            'id': params,
            'attributes': any({})
        });
})