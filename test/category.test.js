const pactum = require('pactum')
const { int, expression } = require('pactum-matchers')

describe('Category operations', () => {
  const e2e = pactum.e2e('Category operations')

  it('Create category', async () => {
    await e2e.step('Post category')
      .spec('Create Category', { '@DATA:TEMPLATE@': 'Category:BasicCategory' })
      .stores('CategoryID', 'data.id')
      .expectJsonMatch({
        '@DATA:TEMPLATE@': 'Category:BasicCategory',
        '@OVERRIDES@': {
          data: {
            id: int()
          }
        }
      })
  })

  it('List categories', async () => {
    await e2e.step('List categories')
      .spec('read')
      .get('/category')
      .expectJsonMatch('data[*].id', expression('$S{CategoryID}', '$V.includes($S{CategoryID})'))
  })

  it('Read category', async () => {
    await e2e.step('Read category')
      .spec('Get Category Tree', '$S{CategoryID}')
      .expectJsonMatch({
        '@DATA:TEMPLATE@': 'Category:BasicCategory',
        '@OVERRIDES@': {
          data: {
            id: int()
          }
        }
      })
  })

  it('Update category', async () => {
    await e2e.step('Update category')
      .spec('update')
      .put('/category/{id}')
      .withPathParams('id', '$S{CategoryID}')
      .withJson({
        '@DATA:TEMPLATE@': 'Category:BasicCategory',
        '@OVERRIDES@': {
          data: {
            attributes: {
              name: 'Salary'
            }
          }
        }
      })
      .expectJson('data.attributes.name', 'Salary')

    await e2e.step('Read updated category')
      .spec('Get Category Tree', '$S{CategoryID}')
      .expectJson('data.attributes.name', 'Salary')
  })

  it('Delete category', async () => {
    await e2e.step('Delete category')
      .spec('delete')
      .delete('/category/{id}')
      .withPathParams('id', '$S{CategoryID}')

    await e2e.step('List categories')
      .spec('read')
      .get('/category')
      .expectJsonMatch('data[*].id', expression('$S{CategoryID}', '!$V.includes($S{CategoryID})'))

    await e2e.cleanup()
  })
})
