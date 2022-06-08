const pactum = require('pactum');

describe('Account flag management', () => {
    const e2e = pactum.e2e('Account operations');

    it('Create account with flags', async () => {
        await e2e.step('Post account')
            .spec('Create Account', { '@DATA:TEMPLATE@': 'Account:Asset:V1' })
            .stores('AccountID', 'id')
            .expectJson('favorite', true)
            .expectJson('operational', true);
    });

    it('List flagged accounts', async () => {
        await e2e.step('List accounts')
            .spec('read')
            .get('/accounts')
            .expectJson('accounts[id=$S{AccountID}].favorite', true)
            .expectJson('accounts[id=$S{AccountID}].operational', true);
    });

    it('Read account with flags', async () => {
        await e2e.step('Read account')
            .spec('read')
            .get('/accounts/{id}')
            .withPathParams('id', '$S{AccountID}')
            .expectJson('favorite', true)
            .expectJson('operational', true);
    });

    it('Update flagged account', async () => {
        await e2e.step('Update flagged account')
            .spec('update')
            .put('/accounts/{id}')
            .withPathParams('id', '$S{AccountID}')
            .withJson({
                '@DATA:TEMPLATE@': 'Account:Asset:V1',
                '@OVERRIDES@': {
                    id: '$S{AccountID}',
                    operational: false,
                    favorite: false
                }
            })
            .expectJson('favorite', false)
            .expectJson('operational', false);

        await e2e.step('Read flagged account')
            .spec('read')
            .get('/accounts/{id}')
            .withPathParams('id', '$S{AccountID}')
            .expectJson('favorite', false)
            .expectJson('operational', false);
        await e2e.cleanup();
    });

    it('Currency change is no allowed for asset accounts', async () => {
        await e2e.step("Try to update currency in asset account")
            .spec('expect error', { statusCode: 422, title: 'ACCOUNT_CURRENCY_ASSET' })
            .put('/accounts/{id}')
            .withPathParams('id', '$S{AccountID}')
            .withJson({
                '@DATA:TEMPLATE@': 'Account:Asset:V1',
                '@OVERRIDES@': {
                    id: '$S{AccountID}',
                    currency_id: 203
                }
            });
    });
});
