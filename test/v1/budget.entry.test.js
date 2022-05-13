const pactum = require('pactum');
const {expression} = require('pactum-matchers');
const {createAccountForTransaction, createUSDExpenseAccountForTransaction} = require('./transaction.handler');

describe('Budget entries operations', () => {
    const e2e = pactum.e2e('Budget operations');

    it('Prepare budget and accounts', async () => {
        await createAccountForTransaction(e2e);
        await e2e.step('Post budget')
            .spec('Create Budget', {'@DATA:TEMPLATE@': 'Budget:Feb:V1'})
            .stores('BudgetID', 'id')
            .clean()
            .delete('/budgets/{id}')
            .withPathParams('id', '$S{BudgetID}');
    });

    it('Budget entries are created for non-asset accounts during budget creation', async () => {
        await e2e.step('List budget entries')
            .spec('read')
            .get('/budgets/{id}/entries')
            .withPathParams('id', '$S{BudgetID}')
            .expectJsonMatch('budget_entries[*].account_id', expression('$S{IncomeAccountID}', '$V.includes($S{IncomeAccountID})'))
            .expectJsonMatch('budget_entries[*].account_id', expression('$S{ExpenseAccountID}', '$V.includes($S{ExpenseAccountID})'))
            .expectJsonMatch('budget_entries[*].account_id', expression('$S{AssetAccountID}', '!$V.includes($S{AssetAccountID})'));
    });

    it('Budget entries are created in existing budget when new account is created', async () => {
        await createUSDExpenseAccountForTransaction(e2e);
        await e2e.step('List budget entries')
            .spec('read')
            .get('/budgets/{id}/entries')
            .withPathParams('id', '$S{BudgetID}')
            .expectJsonMatch('budget_entries[*].account_id', expression('$S{ExpenseUSDAccountID}', '$V.includes($S{ExpenseUSDAccountID})'));
    });

    it('Read budget entry by id', async () => {
        await e2e.step('List budget entries')
            .spec('read')
            .get('/budgets/{id}/entries')
            .withPathParams('id', '$S{BudgetID}')
            .stores('BudgetEntryID', 'budget_entries[0].id')
            .stores('BudgetEntryAccountID', 'budget_entries[0].account_id');

        await e2e.step('Read budget entry')
            .spec('read')
            .get('/budgets/{id}/entries/{entryId}')
            .withPathParams({id: '$S{BudgetID}', entryId: '$S{BudgetEntryID}'})
            .expectJson('id', '$S{BudgetEntryID}');
    });

    it('Update budget entry by id', async () => {
        await e2e.step('Update budget entry')
            .spec('update')
            .put('/budgets/{id}/entries/{entryId}')
            .withPathParams({id: '$S{BudgetID}', entryId: '$S{BudgetEntryID}'})
            .withJson({
                id: '$S{BudgetEntryID}',
                account_id: '$S{BudgetEntryAccountID}',
                even_distribution: true,
                proration: true,
                expected_amount: 9000,
                actual_amount: 150
            })
            .expectJson('expected_amount', 9000);

        await e2e.step('Read budget entry')
            .spec('read')
            .get('/budgets/{id}/entries/{entryId}')
            .withPathParams({id: '$S{BudgetID}', entryId: '$S{BudgetEntryID}'})
            .expectJson('expected_amount', 9000);
    });

    it('Proration follows equal distribution', async () => {
        await e2e.step('Update budget entry')
            .spec('update')
            .put('/budgets/{id}/entries/{entryId}')
            .withPathParams({id: '$S{BudgetID}', entryId: '$S{BudgetEntryID}'})
            .withJson({
                id: '$S{BudgetEntryID}',
                account_id: '$S{BudgetEntryAccountID}',
                account_type: 'income',
                account_name: 'Salary',
                even_distribution: false,
                proration: true,
                expected_amount: 9000,
                actual_amount: 150
            })
            .expectJson('even_distribution', false)
            .expectJson('proration', false);

        await e2e.step('Read budget entry')
            .spec('read')
            .get('/budgets/{id}/entries/{entryId}')
            .withPathParams({id: '$S{BudgetID}', entryId: '$S{BudgetEntryID}'})
            .expectJson('even_distribution', false)
            .expectJson('proration', false);
        await e2e.cleanup();
    });
});
