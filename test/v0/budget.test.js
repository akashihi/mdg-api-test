const pactum = require('pactum');
const { int, expression } = require('pactum-matchers');
const itParam = require('mocha-param');

const INVALID_BUDGETS = [
  {
    id: 'less then two days',
    beginning: '2017-02-09',
    end: '2017-02-09',
    msg: 'BUDGET_SHORT_RANGE'
  },
  {
    id: 'inverted term',
    beginning: '2017-02-10',
    end: '2017-02-09',
    msg: 'BUDGET_INVALID_TERM'
  },
  {
    id: 'fully overlapping term',
    beginning: '2017-02-10',
    end: '2017-02-20',
    msg: 'BUDGET_OVERLAPPING'
  },
  {
    id: 'term overlapping at start',
    beginning: '2017-02-10',
    end: '2017-02-28',
    msg: 'BUDGET_OVERLAPPING'
  },
  {
    id: 'term overlapping at end',
    beginning: '2017-02-01',
    end: '2017-02-10',
    msg: 'BUDGET_OVERLAPPING'
  }
];

describe('Budget operations', () => {
  const e2e = pactum.e2e('Budget operations');

  it.skip('Budget account', async () => {
    await e2e.step('Post budget')
      .spec('Create Budget', { '@DATA:TEMPLATE@': 'Budget:Feb' })
      .stores('BudgetID', 'data.id')
      .expectJsonMatch({
        '@DATA:TEMPLATE@': 'Budget:Feb',
        '@OVERRIDES@': {
          data: {
            id: int()
          }
        }
      });
  });

  it.skip('List budgets', async () => {
    await e2e.step('List budgets')
      .spec('read')
      .get('/budget')
      .expectJsonMatch('data[*].id', expression('$S{BudgetID}', '$V.includes($S{BudgetID})'));
  });

  /*itParam('Budget with ${value.id} is invalid', INVALID_BUDGETS, async (params) => { // eslint-disable-line no-template-curly-in-string
    await e2e.step('Budget validity')
      .spec('expect error', { statusCode: 412, errorCode: params.msg })
      .post('/budget')
      .withJson({
        '@DATA:TEMPLATE@': 'Budget:Feb',
        '@OVERRIDES@': {
          data: {
            attributes: {
              term_beginning: params.beginning,
              term_end: params.end
            }
          }
        }
      });
  });*/

  it.skip('Read budget by date', async () => {
    await e2e.step('Read budget')
      .spec('read')
      .get('/budget/{id}')
      .withPathParams('id', '20170205')
      .expectJsonMatch({
        '@DATA:TEMPLATE@': 'Budget:Feb',
        '@OVERRIDES@': {
          data: {
            id: int()
          }
        }
      });
  });

  it.skip('Delete budget', async () => {
    await e2e.step('Delete budget')
      .spec('delete')
      .delete('/budget/{id}')
      .withPathParams('id', '$S{BudgetID}');

    await e2e.step('List budgets')
      .spec('read')
      .get('/budget')
      .expectJsonMatch('data[*].id', expression('$S{BudgetID}', '!$V.includes($S{BudgetID})'));

    await e2e.cleanup();
  });
});
