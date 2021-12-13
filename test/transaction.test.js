const pactum = require('pactum');
const {int, expression} = require('pactum-matchers');

describe('Transaction operations', () => {
    let e2e = pactum.e2e("Transaction operations")

    it('Create Transaction', async () => {
        await e2e.step('Prepare income account')
            .spec('Create Account', {'@DATA:TEMPLATE@': 'Account:Income'})
            .stores('IncomeAccountID', 'data.id')

        await e2e.step('Prepare asset account')
            .spec('Create Account', {'@DATA:TEMPLATE@': 'Account:Asset'})
            .stores('AssetAccountID', 'data.id')

        await e2e.step('Prepare expense account')
            .spec('Create Account', {'@DATA:TEMPLATE@': 'Account:Expense'})
            .stores('ExpenseAccountID', 'data.id')

        await e2e.step('Post transaction')
            .spec('create')
            .post('/transaction')
            .withJson({'@DATA:TEMPLATE@': 'Transaction:Rent'})
            .stores('TransactionID', 'data.id')
            .expectJsonMatch({
                '@DATA:TEMPLATE@': 'Transaction:Rent',
                '@OVERRIDES@': {
                    data: {
                        id: int()
                    }
                }
            })
    })

//TODO Backend is too slow on non-paged output
    /*it('List transactions', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transaction')
            .expectJsonMatch('data[*].id', expression('$S{TransactionID}', '$V.includes($S{TransactionID})'))
    })*/

    it('Read transaction', async () => {
        await e2e.step('Read transaction')
            .spec('read')
            .get('/transaction/{id}')
            .withPathParams('id', '$S{TransactionID}')
            .expectJsonMatch({
                '@DATA:TEMPLATE@': 'Transaction:Rent',
                '@OVERRIDES@': {
                    data: {
                        id: int()
                    }
                }
            })
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

    it('Delete transaction', async () => {
        await e2e.step('Delete transaction')
            .spec('delete')
            .delete('/transaction/{id}')
            .withPathParams('id', '$S{TransactionID}');

        //TODO backend is too slow to list all transactions
        /*await e2e.step('List transactions')
            .spec('read')
            .get('/transaction')
            .expectJsonMatch('data[*].id', expression('$S{TransactionID}', '$V.includes($S{transaction})'))*/

        await e2e.cleanup()
    })
})
