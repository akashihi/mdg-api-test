const pactum = require('pactum')
const { createAccountForTransaction, createUSDAccountForTransaction } = require('./transaction.handler')

describe('Transaction multi-currency operations', () => {
  const e2e = pactum.e2e('Transaction multi-currency operations')

  it('Create multi-currency transaction', async () => {
    await createAccountForTransaction(e2e)
    await createUSDAccountForTransaction(e2e)

    await e2e.step('Create transaction')
      .spec('Create Transaction', { '@DATA:TEMPLATE@': 'Transaction:MultiCurrency' })
      .stores('TransactionID', 'data.id')
      .expectJsonLike('data.attributes.operations[*].amount', [-100, 200])
      .expectJsonLike('data.attributes.operations[*].rate', [0.5])
  })

  it('Read multi-currency transaction', async () => {
    await e2e.step('Read transaction')
      .spec('read')
      .get('/transaction/{id}')
      .withPathParams('id', '$S{TransactionID}')
      .expectJsonLike('data.attributes.operations[*].amount', [-100, 200])
      .expectJsonLike('data.attributes.operations[*].rate', [0.5])

    await e2e.cleanup()
  })
})
