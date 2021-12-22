const pactum = require('pactum')
const itParam = require('mocha-param')
const { any } = require('pactum-matchers')

const SETTINGS = ['currency.primary', 'ui.transaction.closedialog', 'ui.language']

it('All settings are present in the settings list', async () => {
  await pactum.spec('read')
    .get('/setting')
    .expectJsonLike('data[*].id', SETTINGS)
})

itParam('Loading setting ${value}', SETTINGS, async (params) => { // eslint-disable-line no-template-curly-in-string
  await pactum.spec('read')
    .get('/setting/{id}')
    .withPathParams('id', params)
    .expectJsonMatch('data', {
      type: 'setting',
      id: params,
      attributes: any({})
    })
})

const SETTINGS_TRIGGERS = [
  {
    id: 'ui.transaction.closedialog',
    template: 'Setting:CloseDialog',
    firstValue: 'false',
    secondValue: 'true'
  },
  {
    id: 'currency.primary',
    template: 'Setting:PrimaryCurrency',
    firstValue: '840',
    secondValue: '978'
  }

]

itParam('Check ${value.template} value setting', SETTINGS_TRIGGERS, async (params) => { // eslint-disable-line no-template-curly-in-string
  await pactum.spec('Set setting value', {
    id: params.id,
    value: params.firstValue,
    setting: params.template
  })

  await pactum.spec('Check setting value', { id: params.id, value: params.firstValue })

  await pactum.spec('Set setting value', {
    id: params.id,
    value: params.secondValue,
    setting: params.template
  })

  await pactum.spec('Check setting value', { id: params.id, value: params.secondValue })
})

it('Invalid primary currency is rejected', async () => {
  await pactum.spec('expect error', { statusCode: 422, errorCode: 'SETTING_DATA_INVALID' })
    .put('/setting/{id}')
    .withPathParams('id', 'currency.primary')
    .withJson({
      '@DATA:TEMPLATE@': 'Setting:PrimaryCurrency',
      '@OVERRIDES@': {
        data: {
          attributes: {
            value: '-1'
          }
        }
      }
    })
})
