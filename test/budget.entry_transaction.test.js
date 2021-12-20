const pactum = require('pactum');
const {createAccountForTransaction} = require('./transaction.handler')
const {stash} = require("pactum");

function validateBudgetEntryAmount(expected_amount) {
    return (ctx) => {
        const dataStore = stash.getDataStore();
        const data = ctx.res.json.data;
        const entry = data.filter((item) => item.attributes.account_id === dataStore["ExpenseAccountID"])[0];
        if (entry.attributes.actual_amount !== expected_amount) {
            throw "Actual amount incorrect, expected " + expected_amount + " actual " + entry.attributes.actual_amount;
        }

    }
}
describe('BudgetEntry <-> Transaction operations', () => {
    let e2e = pactum.e2e("Transaction operations")

    it('Prepare budget and accounts', async () => {
        await createAccountForTransaction(e2e)
        await e2e.step('Post budget')
            .spec('Create Budget', {'@DATA:TEMPLATE@': 'Budget:Feb'})
            .stores('BudgetID', 'data.id')
            .clean()
            .delete("/budget/{id}")
            .withPathParams('id', '$S{BudgetID}')
    })

    it('BudgetEntry actual amount is updated after transaction creation', async () => {
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

        await e2e.step('List budget entries')
            .spec('Validate Budget Entry actual amount', 100)
    })


    it('BudgetEntry actual amount is updated after transaction editing', async () => {
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
            .expectJsonLike("data.attributes.operations[*].amount", [-150, 80, 70])

        await e2e.step('List budget entries')
            .spec('Validate Budget Entry actual amount', 70)
    })

    it('BudgetEntry actual amount is reverted after transaction deletion', async () => {
        await e2e.step('Delete transaction')
            .spec('delete')
            .delete('/transaction/{id}')
            .withPathParams('id', '$S{TransactionID}');

        await e2e.step('List budget entries')
            .spec('Validate Budget Entry actual amount', 0)

        await e2e.cleanup()

    })
})
