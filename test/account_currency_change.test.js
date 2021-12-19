const pactum = require('pactum');
const {createAccountForTransaction, createUSDAccountForTransaction} = require('./transaction.handler')

it('Transaction with same currency is rebalanced precisely on currency change', async () => {
    await createAccountForTransaction();
    const usdAccountId = await pactum.spec('Create Account', {'@DATA:TEMPLATE@': 'Account:ExpenseUSD'})
        .stores('AssetUSDAccountID', 'data.id')
        .returns("data.id")

    const transactionId = await pactum.spec('Create Transaction', {'@DATA:TEMPLATE@': 'Transaction:MultiCurrency'})
        .returns("data.id")


    await pactum.spec('update')
        .put('/account/{id}')
        .withPathParams('id', usdAccountId)
        .withJson({
            '@DATA:TEMPLATE@': 'Account:Asset',
            '@OVERRIDES@': {
                data: {
                    attributes: {
                        currency_id: 978
                    }
                }
            }
        })
        .expectJson("data.attributes.currency_id", 978)
        .expectJson("data.attributes.balance", 100)

    await pactum.spec('read')
        .get('/transaction/{id}')
        .withPathParams('id', transactionId)
        .expectJsonLike("data.attributes.operations[*].rate", [1, 1])
}).timeout(15000)