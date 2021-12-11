const pactum = require('pactum');
var itParam = require('mocha-param');
const {any} = require("pactum-matchers");
const stash = pactum.stash;

const SETTINGS = ['currency.primary', 'ui.transaction.closedialog', 'ui.language'];

before(() => {
    stash.addDataTemplate({
        'Setting:CloseDialog': {
            data: {
                type: "setting",
                id: "ui.transaction.closedialog",
                attributes: {
                    value: "true"
                }
            }
        }
    });
});

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

it("Trigger ui.transaction.closedialog", async () => {
    await pactum.spec('Set setting value', {
        id: 'ui.transaction.closedialog',
        value: "false",
        setting: 'Setting:CloseDialog'
    });

    await pactum.spec('Check setting value', {id: 'ui.transaction.closedialog', value: "false"});

    await pactum.spec('Set setting value', {
        id: 'ui.transaction.closedialog',
        value: "true",
        setting: 'Setting:CloseDialog'
    });

    await pactum.spec('Check setting value', {id: 'ui.transaction.closedialog', value: "true"});
})