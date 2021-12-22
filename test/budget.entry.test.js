const pactum = require('pactum')
const { expression } = require('pactum-matchers')
const { createAccountForTransaction, createUSDExpenseAccountForTransaction } = require('./transaction.handler')

describe('Budget operations', () => {
  const e2e = pactum.e2e('Budget operations')

  it('Prepare budget and accounts', async () => {
    await createAccountForTransaction(e2e)
    await e2e.step('Post budget')
      .spec('Create Budget', { '@DATA:TEMPLATE@': 'Budget:Feb' })
      .stores('BudgetID', 'data.id')
      .clean()
      .delete('/budget/{id}')
      .withPathParams('id', '$S{BudgetID}')
  })

  it('Budget entries are created for non-asset accounts during budget creation', async () => {
    await e2e.step('List budget entries')
      .spec('read')
      .get('/budget/{id}/entry')
      .withPathParams('id', '$S{BudgetID}')
      .expectJsonMatch('data[*].attributes.account_id', expression('$S{IncomeAccountID}', '$V.includes($S{IncomeAccountID})'))
      .expectJsonMatch('data[*].attributes.account_id', expression('$S{ExpenseAccountID}', '$V.includes($S{ExpenseAccountID})'))
      .expectJsonMatch('data[*].attributes.account_id', expression('$S{AssetAccountID}', '!$V.includes($S{AssetAccountID})'))
  })

  // TODO Broken on the backend side
  it.skip('Budget entries are created in existing budget when new account is created', async () => {
    const usdAccountId = createUSDExpenseAccountForTransaction(e2e)
    await e2e.step('List budget entries')
      .spec('read')
      .get('/budget/{id}/entry')
      .withPathParams('id', '$S{BudgetID}')
      .expectJsonLike('data[*].attributes.account_id', [usdAccountId])
  })

  it('Read budget entry by id', async () => {
    await e2e.step('List budget entries')
      .spec('read')
      .get('/budget/{id}/entry')
      .withPathParams('id', '$S{BudgetID}')
      .stores('BudgetEntryID', 'data[0].id')
      .stores('BudgetEntryAccountID', 'data[0].attributes.account_id')

    await e2e.step('Read budget entry')
      .spec('read')
      .get('/budget/{id}/entry/{entryId}')
      .withPathParams({ id: '$S{BudgetID}', entryId: '$S{BudgetEntryID}' })
      .expectJson('data.id', '$S{BudgetEntryID}')
      .expectJson('data.type', 'budgetentry')
    await e2e.cleanup()
  })

  // TODO Backend is broken
  it.skip('Update budget entry by id', async () => {
    await e2e.step('Update budget entry')
      .spec('update')
      .put('/budget/{id}/entry/{entryId}')
      .withPathParams({ id: '$S{BudgetID}', entryId: '$S{BudgetEntryID}' })
      .withJson({
        id: '$S{BudgetEntryID}',
        type: 'budgetentry',
        attributes: {
          account_id: '$S{BudgetEntryAccountID}',
          account_type: 'income',
          account_name: 'Salary',
          even_distribution: true,
          proration: true,
          expected_amount: 9000,
          actual_amount: 150
        }
      })
      .expectJson('data.attributes.expected_amount', 9000)

    await e2e.step('Read budget entry')
      .spec('read')
      .get('/budget/{id}/entry/{entryId}')
      .withPathParams({ id: '$S{BudgetID}', entryId: '$S{BudgetEntryID}' })
      .expectJson('data.attributes.expected_amount', 9000)
  })

  // TODO Backend is broken
  it.skip('Proration follows equal distribution', async () => {
    await e2e.step('Update budget entry')
      .spec('update')
      .put('/budget/{id}/entry/{entryId}')
      .withPathParams({ id: '$S{BudgetID}', entryId: '$S{BudgetEntryID}' })
      .withJson({
        id: '$S{BudgetEntryID}',
        type: 'budgetentry',
        attributes: {
          account_id: '$S{BudgetEntryAccountID}',
          account_type: 'income',
          account_name: 'Salary',
          even_distribution: false,
          proration: true,
          expected_amount: 9000,
          actual_amount: 150
        }
      })
      .expectJson('data.attributes.even_distribution', false)
      .expectJson('data.attributes.proration', false)

    await e2e.step('Read budget entry')
      .spec('read')
      .get('/budget/{id}/entry/{entryId}')
      .withPathParams({ id: '$S{BudgetID}', entryId: '$S{BudgetEntryID}' })
      .expectJson('data.attributes.even_distribution', false)
      .expectJson('data.attributes.proration', false)

    await e2e.cleanup()
  })

  /* it('Delete budget', async () => {
        await e2e.step('Delete budget')
            .spec('delete')
            .delete('/budget/{id}')
            .withPathParams('id', '$S{BudgetID}');

        await e2e.step('List budgets')
            .spec('read')
            .get('/budget')
            .expectJsonMatch('data[*].id', expression('$S{BudgetID}', '!$V.includes($S{BudgetID})'))

        await e2e.cleanup()
    }) */
})
