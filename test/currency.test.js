const pactum = require('pactum');
const {like, string, expression} = require('pactum-matchers');
const {describe} = require("mocha/lib/cli/run");

it('Load list of currencies', async () => {
    await pactum.spec('read')
        .get('/currency')
        .expectJsonMatch('data[0]', {
            'type': 'currency',
            'id': like(1),
            'attributes': {
                'name': string(),
                'code': expression(3, '$V.length === 3'),
                'active': like(false)
            }
        });
});

it('Request non-existent currency', async () => {
    /* By definition all currency ids
     * are between 100 and 999 inclusive
     */
    await pactum.spec()
        .get('/currency/1')
        .use('expect error', {status_code: 404, error_code: 'CURRENCY_NOT_FOUND'});
});

pactum.handler.addSpecHandler('Get Currency', (ctx) => {
    const {spec, data} = ctx;
    spec.get('/currency/{id}')
    spec.withPathParams('id', data);
    spec.use('read')
})

pactum.handler.addSpecHandler('Modify Currency', (ctx) => {
    const {spec, data} = ctx;
    const {id, currency} = data;
    spec.put('/currency/{id}')
    spec.withPathParams('id', id);
    spec.withJson(currency)
    spec.use('modification')
})

it('Enable/Disable currency', async () => {
    //Retrieve random currency id
    const currencyID = await pactum.spec('read')
        .get('/currency')
        .returns('data[0].id');

    //Retrieve currency by ID
    const currency_response = await pactum.spec('Get Currency', currencyID);
    let currency = currency_response.json

    //Set active to false and save
    currency.data.attributes.active = false;
    await pactum.spec('Modify Currency', {id: currencyID, currency: currency})
        .expectJson('data.attributes.active', false);

    //Retrieve and check active flag
    await pactum.spec('Get Currency', currencyID)
        .expectJson('data.attributes.active', false);

    //Set active to true and save
    currency.data.attributes.active = true;
    await pactum.spec('Modify Currency', {id: currencyID, currency: currency})
        .expectJson('data.attributes.active', true);

    //Retrieve and check active flag
    await pactum.spec('Get Currency', currencyID)
        .expectJson('data.attributes.active', true);
});