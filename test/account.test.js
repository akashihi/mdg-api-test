const pactum = require('pactum');
const { int, expression } = require('pactum-matchers');

describe('Account operations', () => {
  const e2e = pactum.e2e('Account operations');

  it('Create account', async () => {
    await e2e.step('Post account')
      .spec('Create Account', { '@DATA:TEMPLATE@': 'Account:Expense' })
      .stores('AccountID', 'data.id')
      .expectJsonMatch({
        '@DATA:TEMPLATE@': 'Account:Expense',
        '@OVERRIDES@': {
          data: {
            id: int()
          }
        }
      });
  });

  it('List accounts', async () => {
    await e2e.step('List accounts')
      .spec('read')
      .get('/account')
      .expectJsonMatch('data[*].id', expression('$S{AccountID}', '$V.includes($S{AccountID})'));
  });

  it('Read account', async () => {
    await e2e.step('Read account')
      .spec('read')
      .get('/account/{id}')
      .withPathParams('id', '$S{AccountID}')
      .expectJsonMatch({
        '@DATA:TEMPLATE@': 'Account:Expense',
        '@OVERRIDES@': {
          data: {
            id: int()
          }
        }
      });
  });

  it('Update account', async () => {
    await e2e.step('Update account')
      .spec('update')
      .put('/account/{id}')
      .withPathParams('id', '$S{AccountID}')
      .withJson({
        '@DATA:TEMPLATE@': 'Account:Expense',
        '@OVERRIDES@': {
          data: {
            attributes: {
              name: 'Monthly rent'
            }
          }
        }
      })
      .expectJson('data.attributes.name', 'Monthly rent');

    await e2e.step('Read account')
      .spec('read')
      .get('/account/{id}')
      .withPathParams('id', '$S{AccountID}')
      .expectJson('data.attributes.name', 'Monthly rent');
  });

  it('Hide account', async () => {
    await e2e.step('Hide account')
      .spec('delete')
      .delete('/account/{id}')
      .withPathParams('id', '$S{AccountID}');
  });

  it('Hidden account is in non-filtered account lists', async () => {
    await e2e.step('Hidden account is in non-filtered account lists')
      .spec('read')
      .get('/account')
      .expectJsonMatch('data[*].id', expression('$S{AccountID}', '$V.includes($S{AccountID})'));
  });

  it('Hidden account is in hidden accounts list', async () => {
    await e2e.step('Hidden account is in hidden accounts list')
      .spec('read')
      .get('/account')
      .withQueryParams({ filter: '{"hidden":true}' })
      .expectJsonMatch('data[*].id', expression('$S{AccountID}', '$V.includes($S{AccountID})'));
  });

  it('Hidden account is not in active accounts list', async () => {
    await e2e.step('Hidden account is not in active accounts list')
      .spec('read')
      .get('/account')
      .withQueryParams({ filter: '{"hidden":false}' })
      .expectJsonMatch('data[*].id', expression('$S{AccountID}', '!$V.includes($S{AccountID})'));
  });

  it('Specific filtering ignores hidden flag', async () => {
    await e2e.step('Specific filtering ignores hidden flag')
      .spec('read')
      .get('/account')
      .withQueryParams({ filter: '{"name":"Monthly rent"}' })
      .expectJsonMatch('data[*].id', expression('$S{AccountID}', '$V.includes($S{AccountID})'));

    await e2e.cleanup();
  });
});
