const pactum = require('pactum');

describe('Account primary balance', () => {
    const e2e = pactum.e2e('Account primary balance');

    it('Create account in non-default currency', async () => {
        await e2e.step('Post account')
            .spec('Create Account', {
                '@DATA:TEMPLATE@': 'Account:Income',
                '@OVERRIDES@': {
                    data: {
                        attributes: {
                            currency_id: 840
                        }
                    }
                }
            })
            .stores('IncomeAccountID', 'data.id');

        await e2e.step('Post account')
            .spec('Create Account', {
                '@DATA:TEMPLATE@': 'Account:Asset',
                '@OVERRIDES@': {
                    data: {
                        attributes: {
                            currency_id: 840
                        }
                    }
                }
            })
            .stores('AssetAccountID', 'data.id');
    });

    it('Transaction updates primary balance', async () => {
        await e2e.step('Create transaction')
            .spec('Create Transaction', {'@DATA:TEMPLATE@': 'Transaction:Income'});

        const now = new Date();
        const nowTs = now.toISOString().slice(0, 19);

        const rate = await pactum.spec('read')
            .get('/rate/{ts}/{from}/{to}')
            .withPathParams('ts', nowTs)
            .withPathParams('from', 840)
            .withPathParams('to', 978)
            .returns("data.attributes.rate");

        await e2e.step('Read account')
            .spec('read')
            .get('/account/{id}')
            .withPathParams('id', '$S{IncomeAccountID}')
            .expectJson('data.attributes.primary_balance', -150 * rate);

        await e2e.step('Read account')
            .spec('read')
            .get('/account/{id}')
            .withPathParams('id', '$S{AssetAccountID}')
            .expectJson('data.attributes.primary_balance', 150 * rate);

        await e2e.cleanup();
    });
});
