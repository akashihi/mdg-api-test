const pactum = require('pactum');
const {int, expression} = require('pactum-matchers');
const {createAccountForTransaction, checkAccountsBalances} = require('./transaction.handler')

describe('Transaction operations', () => {
    let e2e = pactum.e2e("Transaction operations")

    it('Create Transaction', async () => {
        await createAccountForTransaction(e2e)

        await e2e.step('Create transaction')
            .spec('Create Transaction', {'@DATA:TEMPLATE@': 'Transaction:Rent'})
            .stores('TransactionID', 'data.id')
            .expectJsonLike({
                '@DATA:TEMPLATE@': 'Transaction:Rent',
                '@OVERRIDES@': {
                    data: {
                        id: 'typeof $V === "number"'
                    }
                }
            })
    })

    it('List transactions', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transaction')
            .expectJsonMatch('data[*].id', expression('$S{TransactionID}', '$V.includes($S{TransactionID})'))
    })

    it('Read transaction', async () => {
        await e2e.step('Read transaction')
            .spec('read')
            .get('/transaction/{id}')
            .withPathParams('id', '$S{TransactionID}')
            .expectJsonLike({
                '@DATA:TEMPLATE@': 'Transaction:Rent',
                '@OVERRIDES@': {
                    data: {
                        id: 'typeof $V === "number"'
                    }
                }
            })
    })

    it('Newly created transaction updates accounts balances', async () => {
        await checkAccountsBalances(e2e, -150, 50, 100)
    })

    it('Update transaction', async () => {
        await e2e.step('Update transaction')
            .spec('update')
            .put('/transaction/{id}')
            .withPathParams('id', '$S{TransactionID}')
            .withJson({
                '@DATA:TEMPLATE@': 'Transaction:Rent',
                '@OVERRIDES@': {
                    data: {
                        attributes: {
                            operations: [
                                {
                                },
                                {
                                    amount: 80
                                },
                                {
                                    amount: 70
                                }
                            ]
                        }
                    }
                }
            })
            .expectJson("data.attributes.operations[1].amount", 80)
            .expectJson("data.attributes.operations[2].amount", 70)

        await e2e.step('Read updated transaction')
            .spec('read')
            .get('/transaction/{id}')
            .withPathParams('id', '$S{TransactionID}')
            .expectJson("data.attributes.operations[1].amount", 80)
            .expectJson("data.attributes.operations[2].amount", 70)
    })

    it('Updated transaction updates accounts balances', async () => {
        await checkAccountsBalances(e2e, -150, 80, 70)
    })

    it('Delete transaction', async () => {
        await e2e.step('Delete transaction')
            .spec('delete')
            .delete('/transaction/{id}')
            .withPathParams('id', '$S{TransactionID}');

        await e2e.step('List transactions')
            .spec('read')
            .get('/transaction')
            .expectJsonMatch('data[*].id', expression('$S{TransactionID}', '!$V.includes($S{TransactionID})'))

    })

    it('Transaction deletion reverts accounts balances', async () => {
        await checkAccountsBalances(e2e, 0, 0, 0)

        await e2e.cleanup()
    })
})
