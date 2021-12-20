const pactum = require('pactum');
const {int, expression} = require('pactum-matchers');

describe('Budget operations', () => {
    let e2e = pactum.e2e("Budget operations")

    it('Budget account', async () => {
        await e2e.step('Post budget')
            .spec('Create Budget', {'@DATA:TEMPLATE@': 'Budget:Feb'})
            .stores('BudgetID', 'data.id')
            .expectJsonMatch({
                '@DATA:TEMPLATE@': 'Budget:Feb', '@OVERRIDES@': {
                    data: {
                        id: int()
                    }
                }
            })
    })

    it('List budgets', async () => {
        await e2e.step('List budgets')
            .spec('read')
            .get('/budget')
            .expectJsonMatch('data[*].id', expression('$S{BudgetID}', '$V.includes($S{BudgetID})'))
    })

    it('Read budget by date', async () => {
        await e2e.step('Read budget')
            .spec('read')
            .get("/budget/{id}")
            .withPathParams('id', '20170205')
            .expectJsonMatch({
                '@DATA:TEMPLATE@': 'Budget:Feb', '@OVERRIDES@': {
                    data: {
                        id: int()
                    }
                }
            })
    })

    it('Delete budget', async () => {
        await e2e.step('Delete budget')
            .spec('delete')
            .delete('/budget/{id}')
            .withPathParams('id', '$S{BudgetID}');

        await e2e.step('List budgets')
            .spec('read')
            .get('/budget')
            .expectJsonMatch('data[*].id', expression('$S{BudgetID}', '!$V.includes($S{BudgetID})'))

        await e2e.cleanup()
    })
})