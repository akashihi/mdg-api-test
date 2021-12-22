const pactum = require('pactum')
const { createAccountForTransaction } = require('./transaction.handler')

function makeTag (length) {
  let result = ''
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  const charactersLength = characters.length
  for (let i = 0; i < length; i++) {
    result += characters.charAt(Math.floor(Math.random() *
            charactersLength))
  }
  return result
}

it('Tag retrieval', async () => {
  await createAccountForTransaction()

  const firstTag = makeTag(8)
  const secondTag = makeTag(16)

  await pactum.spec('Create Transaction', {
    '@DATA:TEMPLATE@': 'Transaction:Rent',
    '@OVERRIDES@': {
      data: {
        attributes: {
          tags: [firstTag, secondTag]
        }
      }
    }
  })

  await pactum.spec('read')
    .get('/tag')
    .expectJsonLike('data[*].attributes.txtag', [firstTag, secondTag])
})
