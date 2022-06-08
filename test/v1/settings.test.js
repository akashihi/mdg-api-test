const pactum = require('pactum');
const itParam = require('mocha-param');

const SETTINGS = ['currency.primary', 'ui.transaction.closedialog', 'ui.language'];

it('All settings are present in the settings list', async () => {
    await pactum.spec('read')
        .get('/settings')
        .expectJsonLike('settings[*].id', SETTINGS);
});

itParam('Loading setting ${value}', SETTINGS, async (params) => { // eslint-disable-line no-template-curly-in-string
    await pactum.spec('read')
        .get('/settings/{id}')
        .withPathParams('id', params)
        .expectJson("id", params);
});

const SETTINGS_TRIGGERS = [
    {
        id: 'ui.transaction.closedialog',
        firstValue: 'false',
        secondValue: 'true'
    },
    {
        id: 'currency.primary',
        firstValue: '840',
        secondValue: '978'
    }

];

itParam('Check ${value.id} value setting', SETTINGS_TRIGGERS, async (params) => { // eslint-disable-line no-template-curly-in-string
    await pactum.spec('Set setting value', {
        id: params.id,
        value: params.firstValue
    });

    await pactum.spec('Check setting value', { id: params.id, value: params.firstValue });

    await pactum.spec('Set setting value', {
        id: params.id,
        value: params.secondValue
    });

    await pactum.spec('Check setting value', { id: params.id, value: params.secondValue });
});

it('Invalid primary currency is rejected', async () => {
    await pactum.spec('expect error', { statusCode: 422, title: 'SETTING_DATA_INVALID' })
        .put('/settings/{id}')
        .withPathParams('id', 'currency.primary')
        .withJson({
            id: 'currency.primary',
            value: -1
        });
});
