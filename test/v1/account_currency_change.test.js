const pactum = require('pactum');
const {createAccountForTransaction} = require('./transaction.handler');

it('Transaction with same currency is rebalanced precisely on currency change', async () => {
    await createAccountForTransaction();
    const usdAccountId = await pactum.spec('Create Account', {'@DATA:TEMPLATE@': 'Account:Expense:USD:V1'})
        .stores('AssetUSDAccountID', 'id')
        .returns('id');

    const transactionId = await pactum.spec('Create Transaction', {'@DATA:TEMPLATE@': 'Transaction:MultiCurrency:V1'})
        .returns('id');

    await pactum.spec('update')
        .put('/accounts/{id}')
        .withPathParams('id', usdAccountId)
        .withJson({
            '@DATA:TEMPLATE@': 'Account:Expense:V1',
            '@OVERRIDES@': {
                currency_id: 978
            }
        })
        .expectJson('currency_id', 978)
        .expectJson('balance', 100);

    await pactum.spec('read')
        .get('/transactions/{id}')
        .withPathParams('id', transactionId)
        .expectJsonLike('operations[*].rate', [1, 1]);
}).timeout(15000);

it('Transaction with default currency is rebalanced correctly on currency change', async () => {
    const accountId = await pactum.spec('Create Account', {'@DATA:TEMPLATE@': 'Account:Expense:V1'})
        .stores('AssetAccountID', 'id')
        .returns('id');
    await pactum.spec('Create Account', {'@DATA:TEMPLATE@': 'Account:Expense:USD:V1'})
        .stores('AssetUSDAccountID', 'id')
        .returns('id');

    const transactionId = await pactum.spec('Create Transaction', {'@DATA:TEMPLATE@': 'Transaction:MultiCurrency:V1'})
        .returns('id');

    await pactum.spec('update')
        .put('/accounts/{id}')
        .withPathParams('id', accountId)
        .withJson({
            '@DATA:TEMPLATE@': 'Account:Expense:V1',
            '@OVERRIDES@': {
                currency_id: 840
            }
        })
        .expectJson('currency_id', 840)
        .expectJson('balance', -200);

    await pactum.spec('read')
        .get('/transactions/{id}')
        .withPathParams('id', transactionId)
        .expectJsonLike('operations[*].rate', [1, 1]);
}).timeout(15000);

it('Transaction with multiple currencies have rate recalculated', async () => {
    const usdAccountId = await pactum.spec('Create Account', {'@DATA:TEMPLATE@': 'Account:Expense:USD:V1'})
        .stores('AssetUSDAccountID', 'id')
        .returns('id');
    const eurAccountId = await pactum.spec('Create Account', {
        '@DATA:TEMPLATE@': 'Account:Expense:USD:V1',
        '@OVERRIDES@': {
            currency_id: 978
        }
    })
        .stores('AssetEURAccountID', 'id')
        .returns('id');

    const czkAccountId = await pactum.spec('Create Account', {
        '@DATA:TEMPLATE@': 'Account:Expense:USD:V1',
        '@OVERRIDES@': {
            currency_id: 203
        }
    })
        .stores('AssetCZKAccountID', 'id')
        .returns('id');

    await pactum.spec('Create Transaction', {
                "timestamp": "2017-02-06T16:45:36",
                "comment": "Multi currency",
                "tags": [
                    "test",
                    "transaction"
                ],
                "operations": [
                    {
                        "account_id": eurAccountId,
                        "amount": 100,
                        "rate": 25
                    },
                    {
                        "account_id": usdAccountId,
                        "rate": 20,
                        "amount": 100
                    },
                    {
                        "account_id": czkAccountId,
                        "rate": 1,
                        "amount": -4500
                    }
                ]
    })
        .stores('TransactionID', 'id');

    await pactum.spec('update')
        .put('/accounts/{id}')
        .withPathParams('id', czkAccountId)
        .withJson({
            '@DATA:TEMPLATE@': 'Account:Expense:V1',
            '@OVERRIDES@': {
                currency_id: 840
            }
        })
        .expectJson('currency_id', 840)
        .expectJson('balance', -219);

    await pactum.spec('read')
        .get('/accounts/{id}')
        .withPathParams('id', usdAccountId)
        .expectJson('balance', 100);

    await pactum.spec('read')
        .get('/accounts/{id}')
        .withPathParams('id', eurAccountId)
        .expectJson('balance', 100);

    await pactum.spec('read')
        .get('/transactions/{id}')
        .withPathParams('id', '$S{TransactionID}')
        .expectJsonLike('operations[*].rate', [1.19, 1, 1]);
}).timeout(15000);
