const pactum = require("pactum");
const {createAccountForTransaction} = require("./transaction.handler");
const {stash} = require("pactum");
describe('Budget <-> entry calculations', () => {
    const e2e = pactum.e2e('Budget <-> entry calculations');

    it('Prepare budget and accounts', async () => {
        await createAccountForTransaction(e2e);
        await e2e.step('Post budget')
            .spec('Create Budget', {'@DATA:TEMPLATE@': 'Budget:Feb:V1'})
            .stores('BudgetID', 'id')
            .clean()
            .delete('/budgets/{id}')
            .withPathParams('id', '$S{BudgetID}');
    });

    it('Load income/expense entries', async () => {
        const entries = await pactum.spec('read')
            .get('/budgets/{id}/entries')
            .withPathParams('id', '$S{BudgetID}')
            .returns("budget_entries[*]");
        const dataStore = stash.getDataStore();

        const incomeAccountID = dataStore.IncomeAccountID;
        const expenseAccountId = dataStore.ExpenseAccountID;

        const incomeEntryID = entries.filter(e => e.account_id === incomeAccountID)[0].id;
        const expenseEntryID = entries.filter(e => e.account_id === expenseAccountId)[0].id;
        stash.getDataStore().incomeEntryID = incomeEntryID;
        stash.getDataStore().expenseEntryID = expenseEntryID;
    });

    it("Budget expected income should follow entries expected income", async() => {
        await e2e.step('Update income budget entry')
            .spec('update')
            .put('/budgets/{id}/entries/{entryId}')
            .withPathParams({id: '$S{BudgetID}', entryId: '$S{incomeEntryID}'})
            .withJson({
                id: '$S{incomeEntryID}',
                account_id: '$S{IncomeAccountID}',
                even_distribution: false,
                proration: false,
                expected_amount: 9000,
                actual_amount: 150
            })
            .expectJson('expected_amount', 9000);

        await e2e.step('Read budget')
            .spec('read')
            .get('/budgets/{id}')
            .withPathParams('id', '20170205')
            .expectJsonMatch('state.income.expected', 9000);
    });

    it("Budget expected expense should follow entries expected expense", async() => {
        await e2e.step('Update income budget entry')
            .spec('update')
            .put('/budgets/{id}/entries/{entryId}')
            .withPathParams({id: '$S{BudgetID}', entryId: '$S{expenseEntryID}'})
            .withJson({
                id: '$S{expenseEntryID}',
                account_id: '$S{ExpenseAccountID}',
                even_distribution: false,
                proration: false,
                expected_amount: 100500,
                actual_amount: 150
            })
            .expectJson('expected_amount', 100500);

        await e2e.step('Read budget')
            .spec('read')
            .get('/budgets/{id}')
            .withPathParams('id', '20170205')
            .expectJsonMatch('state.expense.expected', 100500);
    });

    it("Budget actual totals follow actual transactions", async() => {
        await e2e.step('Read initial budget values')
            .spec('read')
            .get('/budgets/{id}')
            .withPathParams('id', '20170205')
            .stores('BudgetActualIncome', 'state.income.actual')
            .stores('BudgetActualExpense', 'state.expense.actual');

        await e2e.step('Create transaction')
            .spec('Create Transaction', {'@DATA:TEMPLATE@': 'Transaction:Rent:V1'})
            .stores('TransactionID', 'id');

        const budgetActualIncome = stash.getDataStore().BudgetActualIncome;
        const budgetActualExpense = stash.getDataStore().BudgetActualExpense;
        await e2e.step('Read budget')
            .spec('read')
            .get('/budgets/{id}')
            .withPathParams('id', '20170205')
            .expectJsonMatch('state.income.actual', budgetActualIncome + 150)
            .expectJsonMatch('state.expense.actual', budgetActualExpense + 100);
        await e2e.cleanup();
    }).timeout(15000);
});