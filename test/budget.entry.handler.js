const pactum = require("pactum");
const {stash} = require("pactum");

function validateBudgetEntryAmount(expected_amount) {
    return (ctx) => {
        const dataStore = stash.getDataStore();
        const data = ctx.res.json.data;
        const entry = data.filter((item) => item.attributes.account_id === dataStore["ExpenseAccountID"])[0];
        if (entry.attributes.actual_amount !== expected_amount) {
            throw "Actual amount incorrect, expected " + expected_amount + " actual " + entry.attributes.actual_amount;
        }

    }
}

pactum.handler.addSpecHandler('Validate Budget Entry actual amount', (ctx) => {
    const {spec, data} = ctx;
    spec.use('read')
    spec.get('/budget/{id}/entry')
    spec.withPathParams('id', '$S{BudgetID}')
        .expect(validateBudgetEntryAmount(data))
})
