const pactum = require('pactum');
const { like, string, expression } = require('pactum-matchers');

it('Load list of currencies', async () => {
    await pactum.spec('read')
        .get('/currencies')
        .expectJsonMatch('currencies[0]', {
            name: string(),
            code: expression(3, '$V.length === 3'),
            active: like(false)
        });
});

it('Request non-existent currency', async () => {
    /* By definition all currency ids
       * are between 100 and 999 inclusive
       */
    await pactum.spec('expect error', { statusCode: 404, title: 'CURRENCY_NOT_FOUND', instance: '/currencies/1'})
        .get('/currencies/1');
});

it('Enable/Disable currency', async () => {
    // Bolivian boliviano is not used in the other tests
    const currencyID = '068';

    // Retrieve currency by ID
    const currencyResponse = await pactum.spec('Get Currency', currencyID);
    const currency = currencyResponse.json;

    // Set active to true and save
    currency.active = true;
    await pactum.spec('Modify Currency', { id: currencyID, currency: currency })
        .expectJson('active', true);

    // Retrieve and check active flag
    await pactum.spec('Get Currency', currencyID)
        .expectJson('active', true);

    // Set active to false and save
    currency.active = false;
    await pactum.spec('Modify Currency', { id: currencyID, currency: currency })
        .expectJson('active', false);

    // Retrieve and check active flag
    await pactum.spec('Get Currency', currencyID)
        .expectJson('active', false);
});
