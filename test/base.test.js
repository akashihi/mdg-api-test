const { request, stash } = require('pactum')

// load handlers
require('./error.handler')
require('./op.handler')
require('./currency.handler')
require('./settings.handler')
require('./category.handler')
require('./transaction.handler')
require('./budget.handler')
require('./budget.entry.handler')

// global hook
before(() => {
  stash.loadData()
  request.setBaseUrl('http://localhost:9000/api')
})
