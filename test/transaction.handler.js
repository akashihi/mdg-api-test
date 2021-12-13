const pactum = require("pactum");

pactum.handler.addSpecHandler('Create Transaction', (ctx) => {
    const {spec, data} = ctx;
    spec.post('/transaction')
    spec.withJson(data)
    spec.use('create')
})

async function createAccountForTransaction(e2e) {
    let firstStep = pactum;
    let secondStep = pactum;
    let thirdStep = pactum;

    if (e2e) {
        firstStep = e2e.step('Prepare income account');
        secondStep = e2e.step('Prepare asset account');
        thirdStep = e2e.step('Prepare expense account')
    }
    await firstStep.spec('Create Account', {'@DATA:TEMPLATE@': 'Account:Income'})
        .stores('IncomeAccountID', 'data.id')

    await secondStep.spec('Create Account', {'@DATA:TEMPLATE@': 'Account:Asset'})
        .stores('AssetAccountID', 'data.id')

    await thirdStep.spec('Create Account', {'@DATA:TEMPLATE@': 'Account:Expense'})
        .stores('ExpenseAccountID', 'data.id')
}

module.exports = { createAccountForTransaction }
