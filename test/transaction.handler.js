const pactum = require('pactum');

pactum.handler.addSpecHandler('Create Transaction', (ctx) => {
  const { spec, data } = ctx;
  spec.post('/transaction');
  spec.withJson(data);
  spec.use('create');
});

async function createAccountForTransaction (e2e) {
  let firstStep = pactum;
  let secondStep = pactum;
  let thirdStep = pactum;

  if (e2e) {
    firstStep = e2e.step('Prepare income account');
    secondStep = e2e.step('Prepare asset account');
    thirdStep = e2e.step('Prepare expense account');
  }
  await firstStep.spec('Create Account', { '@DATA:TEMPLATE@': 'Account:Income' })
    .stores('IncomeAccountID', 'data.id');

  await secondStep.spec('Create Account', { '@DATA:TEMPLATE@': 'Account:Asset' })
    .stores('AssetAccountID', 'data.id');

  await thirdStep.spec('Create Account', { '@DATA:TEMPLATE@': 'Account:Expense' })
    .stores('ExpenseAccountID', 'data.id');
}

async function checkAccountsBalances (e2e, income, assets, expense) {
  await e2e.step('Read income account')
    .spec('Validate account balance', { id: '$S{IncomeAccountID}', balance: income });

  await e2e.step('Read asset account')
    .spec('Validate account balance', { id: '$S{AssetAccountID}', balance: assets });

  await e2e.step('Read expense account')
    .spec('Validate account balance', { id: '$S{ExpenseAccountID}', balance: expense });
}

async function createUSDAccountForTransaction (e2e) {
  let usdStep = pactum;

  if (e2e) {
    usdStep = e2e.step('Prepare USD asset account');
  }

  return usdStep
    .spec('Create Account', { '@DATA:TEMPLATE@': 'Account:AssetUSD' })
    .stores('AssetUSDAccountID', 'data.id')
    .returns('data.id');
}

async function createUSDExpenseAccountForTransaction (e2e) {
  let usdStep = pactum;

  if (e2e) {
    usdStep = e2e.step('Prepare USD expense account');
  }

  return usdStep
    .spec('Create Account', { '@DATA:TEMPLATE@': 'Account:ExpenseUSD' })
    .stores('ExpenseUSDAccountID', 'data.id')
    .returns('data.id');
}

module.exports = { createAccountForTransaction, checkAccountsBalances, createUSDAccountForTransaction, createUSDExpenseAccountForTransaction };
