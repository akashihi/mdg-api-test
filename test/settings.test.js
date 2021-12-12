const pactum = require('pactum');
var itParam = require('mocha-param');
const {any} = require("pactum-matchers");
const stash = pactum.stash;
var itParam = require('mocha-param');

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
    stash.addDataTemplate({
        'Setting:PrimaryCurrency': {
            data: {
                type: "setting",
                id: "currency.primary",
                attributes: {
                    value: "978"
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

const SETTINGS_TRIGGERS = [
    {
        id : 'ui.transaction.closedialog',
        template: 'Setting:CloseDialog',
        firstValue : "false",
        secondValue: "true"
    },
    {
        id : 'currency.primary',
        template: 'Setting:PrimaryCurrency',
        firstValue : "840",
        secondValue: "978"
    }

]

itParam("Check ${value.template} value setting", SETTINGS_TRIGGERS, async (params) => {
    await pactum.spec('Set setting value', {
        id: params.id,
        value: params.firstValue,
        setting: params.template
    });

    await pactum.spec('Check setting value', {id: params.id, value: params.firstValue});

    await pactum.spec('Set setting value', {
        id: params.id,
        value: params.secondValue,
        setting: params.template
    });

    await pactum.spec('Check setting value', {id: params.id, value: params.secondValue});
})

it("Invalid primary currency is rejected", async () => {
    await pactum.spec('expect error', {status_code: 422, error_code: 'SETTING_DATA_INVALID'})
        .put('/setting/{id}')
        .withPathParams('id', 'currency.primary')
        .withJson({
        '@DATA:TEMPLATE@': 'Setting:PrimaryCurrency',
        '@OVERRIDES@': {
            data: {
                attributes: {
                    value: "-1"
                }
            }
        }
    })
})