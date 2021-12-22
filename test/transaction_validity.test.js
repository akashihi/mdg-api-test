const pactum = require('pactum')
const { createAccountForTransaction, createUSDAccountForTransaction } = require('./transaction.handler')

it('Empty transactions are not allowed', async () => {
  await createAccountForTransaction()

  // No way to remove field from the template
  await pactum.spec('expect error', { statusCode: 412, errorCode: 'TRANSACTION_EMPTY' })
    .post('/transaction')
    .withJson({
      data: {
        attributes: {
          timestamp: '2017-02-04T16:45:36',
          comment: 'Test transaction',
          tags: [
            'test',
            'transaction'
          ],
          operations: []
        },
        type: 'transaction'
      }
    })
})

it('Empty operations are ignored', async () => {
  await createAccountForTransaction()

  await pactum.spec('expect error', { statusCode: 412, errorCode: 'TRANSACTION_EMPTY' })
    .post('/transaction')
    .withJson({
      '@DATA:TEMPLATE@': 'Transaction:Rent',
      '@OVERRIDES@': {
        data: {
          attributes: {
            operations: [
              {
                amount: 0
              },
              {
                amount: 0
              },
              {
                amount: 0
              }
            ]
          }
        }
      }
    })
})

it('Unbalanced transactions are not allowed', async () => {
  await createAccountForTransaction()

  await pactum.spec('expect error', { statusCode: 412, errorCode: 'TRANSACTION_NOT_BALANCED' })
    .post('/transaction')
    .withJson({
      '@DATA:TEMPLATE@': 'Transaction:Rent',
      '@OVERRIDES@': {
        data: {
          attributes: {
            operations: [
              {
                amount: -50
              },
              {
                amount: 100
              },
              {
                amount: 25
              }
            ]
          }
        }
      }
    })
})

it('Multi currency transaction without rate are not allowed', async () => {
  await createAccountForTransaction()
  await createUSDAccountForTransaction()

  // No way to remove field from the template
  await pactum.spec('expect error', { statusCode: 412, errorCode: 'TRANSACTION_AMBIGUOUS_RATE' })
    .post('/transaction')
    .withJson({
      data: {
        attributes: {
          timestamp: '2017-02-05T13:54:35',
          comment: 'Test transaction',
          tags: [
            'test',
            'transaction'
          ],
          operations: [
            {
              account_id: '$S{AssetAccountID}',
              amount: -100
            },
            {
              account_id: '$S{AssetUSDAccountID}',
              amount: 200
            }
          ]
        },
        type: 'transaction'
      }
    })
})

it('Multi currency transaction with rate set to all operations are not allowed', async () => {
  await createAccountForTransaction()
  await createUSDAccountForTransaction()

  // No way to remove field from the template
  await pactum.spec('expect error', { statusCode: 412, errorCode: 'TRANSACTION_NO_DEFAULT_RATE' })
    .post('/transaction')
    .withJson({
      '@DATA:TEMPLATE@': 'Transaction:MultiCurrency',
      '@OVERRIDES@': {
        data: {
          attributes: {
            operations: [
              {
                rate: 3
              }
            ]
          }
        }
      }
    })
})

it('Multi currency transaction with default rate on different currencies are not allowed', async () => {
  await createAccountForTransaction()
  await createUSDAccountForTransaction()

  // No way to remove field from the template
  await pactum.spec('expect error', { statusCode: 412, errorCode: 'TRANSACTION_AMBIGUOUS_RATE' })
    .post('/transaction')
    .withJson({
      '@DATA:TEMPLATE@': 'Transaction:MultiCurrency',
      '@OVERRIDES@': {
        data: {
          attributes: {
            operations: [
              {
              },
              {
                rate: 1 // The upper operation has no rate (=default rate) and this is forced to default rate value too
              }
            ]
          }
        }
      }
    })
})

it('Multi currency transaction with 0 rate is not allowed', async () => {
  await createAccountForTransaction()
  await createUSDAccountForTransaction()

  // No way to remove field from the template
  await pactum.spec('expect error', { statusCode: 412, errorCode: 'TRANSACTION_ZERO_RATE' })
    .post('/transaction')
    .withJson({
      '@DATA:TEMPLATE@': 'Transaction:MultiCurrency',
      '@OVERRIDES@': {
        data: {
          attributes: {
            operations: [
              {
              },
              {
                rate: 0
              }
            ]
          }
        }
      }
    })
})

it('Unbalanced multi currency transactions are not allowed', async () => {
  await createAccountForTransaction()
  await createUSDAccountForTransaction()

  // No way to remove field from the template
  await pactum.spec('expect error', { statusCode: 412, errorCode: 'TRANSACTION_NOT_BALANCED' })
    .post('/transaction')
    .withJson({
      '@DATA:TEMPLATE@': 'Transaction:MultiCurrency',
      '@OVERRIDES@': {
        data: {
          attributes: {
            operations: [
              {
                amount: -120
              }
            ]
          }
        }
      }
    })
})
