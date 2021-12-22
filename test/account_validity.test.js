const pactum = require('pactum')

it('Currency change is no allowed for asset accounts', async () => {
  const accountId = await pactum.spec('Create Account', { '@DATA:TEMPLATE@': 'Account:Asset' })
    .returns('data.id')

  await pactum.spec('expect error', { statusCode: 422, errorCode: 'ACCOUNT_CURRENCY_ASSET' })
    .put('/account/{id}')
    .withPathParams('id', accountId)
    .withJson({
      '@DATA:TEMPLATE@': 'Account:Asset',
      '@OVERRIDES@': {
        data: {
          attributes: {
            currency_id: 203
          }
        }
      }
    })
})
