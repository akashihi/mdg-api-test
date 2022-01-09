const pactum = require('pactum');

describe('Account primary balance', () => {
    const e2e = pactum.e2e('Account primary balance');

    it('Create account in non-default currency', async () => {
        await e2e.step('Post account')
            .spec('Create Account', {
                '@DATA:TEMPLATE@': 'Account:Income:V1',
                '@OVERRIDES@': {
                    currency_id: 840
                }
            })
            .stores('IncomeAccountID', 'id');

        await e2e.step('Post account')
            .spec('Create Account', {
                '@DATA:TEMPLATE@': 'Account:Asset:V1',
                '@OVERRIDES@': {
                    currency_id: 840
                }
            })
            .stores('AssetAccountID', 'id');
    });

    it('Transaction updates primary balance', async () => {
        await e2e.step('Create transaction')
            .spec('Create Transaction', { '@DATA:TEMPLATE@': 'Transaction:Income:V1' });

        await e2e.step('Read account')
            .spec('read')
            .get('/accounts/{id}')
            .withPathParams('id', '$S{IncomeAccountID}')
            .expectJson('primary_balance', -132);

        await e2e.step('Read account')
            .spec('read')
            .get('/accounts/{id}')
            .withPathParams('id', '$S{AssetAccountID}')
            .expectJson('primary_balance', 132);

        await e2e.cleanup();
    });
});
