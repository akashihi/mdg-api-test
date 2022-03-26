const pactum = require('pactum');
const {createAccountForTransaction} = require('./transaction.handler');

it.skip('Transaction with same currency is rebalanced precisely on currency change', async () => {
    await createAccountForTransaction();
    const usdAccountId = await pactum.spec('Create Account', {'@DATA:TEMPLATE@': 'Account:ExpenseUSD'})
        .stores('AssetUSDAccountID', 'data.id')
        .returns('data.id');

    const transactionId = await pactum.spec('Create Transaction', {'@DATA:TEMPLATE@': 'Transaction:MultiCurrency'})
        .returns('data.id');

    await pactum.spec('update')
        .put('/account/{id}')
        .withPathParams('id', usdAccountId)
        .withJson({
            '@DATA:TEMPLATE@': 'Account:Expense',
            '@OVERRIDES@': {
                data: {
                    attributes: {
                        currency_id: 978
                    }
                }
            }
        })
        .expectJson('data.attributes.currency_id', 978)
        .expectJson('data.attributes.balance', 100);

    await pactum.spec('read')
        .get('/transaction/{id}')
        .withPathParams('id', transactionId)
        .expectJsonLike('data.attributes.operations[*].rate', [1, 1]);
}).timeout(15000);

it.skip('Transaction with default currency is rebalanced correctly on currency change', async () => {
    const accountId = await pactum.spec('Create Account', {'@DATA:TEMPLATE@': 'Account:Expense'})
        .stores('AssetAccountID', 'data.id')
        .returns('data.id');
    await pactum.spec('Create Account', {'@DATA:TEMPLATE@': 'Account:ExpenseUSD'})
        .stores('AssetUSDAccountID', 'data.id')
        .returns('data.id');

    const transactionId = await pactum.spec('Create Transaction', {'@DATA:TEMPLATE@': 'Transaction:MultiCurrency'})
        .returns('data.id');

    await pactum.spec('update')
        .put('/account/{id}')
        .withPathParams('id', accountId)
        .withJson({
            '@DATA:TEMPLATE@': 'Account:Asset',
            '@OVERRIDES@': {
                data: {
                    attributes: {
                        currency_id: 840
                    }
                }
            }
        })
        .expectJson('data.attributes.currency_id', 840)
        .expectJson('data.attributes.balance', 203);

    await pactum.spec('read')
        .get('/transaction/{id}')
        .withPathParams('id', transactionId)
        .expectJsonLike('data.attributes.operations[*].rate', [1, 1]);
}).timeout(15000);

it.skip('Transaction with multiple currencies have rate recalculated', async () => {
    const usdAccountId = await pactum.spec('Create Account', {'@DATA:TEMPLATE@': 'Account:ExpenseUSD'})
        .stores('AssetUSDAccountID', 'data.id')
        .returns('data.id');
    const eurAccountId = await pactum.spec('Create Account', {
        '@DATA:TEMPLATE@': 'Account:ExpenseUSD',
        '@OVERRIDE': {
            data: {
                attributes: {
                    currency_id: 978
                }
            }
        }
    })
        .stores('AssetEURAccountID', 'data.id')
        .returns('data.id');

    const czkAccountId = await pactum.spec('Create Account', {
        '@DATA:TEMPLATE@': 'Account:ExpenseUSD',
        '@OVERRIDE': {
            data: {
                attributes: {
                    currency_id: 203
                }
            }
        }
    })
        .stores('AssetCZKAccountID', 'data.id')
        .returns('data.id');

    await pactum.spec('Create Transaction', {
        "data": {
            "attributes": {
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
            },
            "type": "transaction"
        }
    })
        .stores('TransactionID', 'data.id');

    await pactum.spec('update')
        .put('/account/{id}')
        .withPathParams('id', czkAccountId)
        .withJson({
            '@DATA:TEMPLATE@': 'Account:Expense',
            '@OVERRIDES@': {
                data: {
                    attributes: {
                        currency_id: 840
                    }
                }
            }
        })
        .expectJson('data.attributes.currency_id', 840)
        .expectJson('data.attributes.balance', -219);

    await pactum.spec('read')
        .get('/account/{id}')
        .withPathParams('id', usdAccountId)
        .expectJson('data.attributes.balance', -219);

    await pactum.spec('read')
        .get('/account/{id}')
        .withPathParams('id', eurAccountId)
        .expectJson('data.attributes.balance', -219);

    await pactum.spec('read')
        .get('/transaction/{id}')
        .withPathParams('id', '$S{TransactionID}')
        .expectJsonLike('data.attributes.operations[*].rate', [1.19, 1, 1]);
}).timeout(15000);
