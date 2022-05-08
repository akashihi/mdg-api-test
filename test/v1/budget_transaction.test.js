const pactum = require('pactum');
const {createAccountForTransaction} = require('./transaction.handler');

describe('Budget <-> Transaction operations', () => {
    const e2e = pactum.e2e('Budget <-> Transaction operations');

    it('Prepare budget and accounts', async () => {
        await createAccountForTransaction(e2e);
        await e2e.step('Post budget')
            .spec('Create Budget', {'@DATA:TEMPLATE@': 'Budget:Feb:V1'})
            .stores('BudgetID', 'id')
            .stores('BudgetActualAmount', 'outgoing_amount.actual')
            .clean()
            .delete('/budgets/{id}')
            .withPathParams('id', '$S{BudgetID}');
    });

    it('Budget actual amount is updated after transaction creation', async () => {
        await e2e.step('Create transaction')
            .spec('Create Transaction', {'@DATA:TEMPLATE@': 'Transaction:Rent:V1'})
            .stores('TransactionID', 'id');

        await e2e.step('Read budget')
            .spec('Validate Budget actual amount', 50);
    });

    it('BudgetEntry actual amount is updated after transaction editing', async () => {
        await e2e.step('Update transaction')
            .spec('update')
            .put('/transactions/{id}')
            .withPathParams('id', '$S{TransactionID}')
            .withJson({
                '@DATA:TEMPLATE@': 'Transaction:Rent:V1',
                '@OVERRIDES@': {
                    operations: [
                        {},
                        {
                            amount: 80
                        },
                        {
                            amount: 70
                        }
                    ]
                }
            });

        await e2e.step('Read budget')
            .spec('Validate Budget actual amount', 80);
    });

    it('BudgetEntry actual amount is reverted after transaction deletion', async () => {
        await e2e.step('Delete transaction')
            .spec('delete')
            .delete('/transactions/{id}')
            .withPathParams('id', '$S{TransactionID}');

        await e2e.step('Read budget')
            .spec('Validate Budget actual amount', 0);

        await e2e.cleanup();
    });
});
