const pactum = require('pactum')
const { stash } = require('pactum')

function validateBudgetAmount (difference) {
  return (ctx) => {
    const dataStore = stash.getDataStore()
    const actual = ctx.res.json.data.attributes.outgoing_amount.actual
    const expected = dataStore.BudgetActualAmount + difference
    if (actual !== expected) {
      throw new Error('Actual amount incorrect, expected ' + expected + ' actual ' + actual)
    }
  }
}

pactum.handler.addSpecHandler('Validate Budget actual amount', (ctx) => {
  const { spec, data } = ctx
  spec.use('read')
  spec.get('/budget/{id}')
  spec.withPathParams('id', '$S{BudgetID}')
    .expect(validateBudgetAmount(data))
})

pactum.handler.addSpecHandler('Create Budget', (ctx) => {
  const { spec, data } = ctx
  spec.post('/budget')
  spec.withJson(data)
  spec.use('create')
})
