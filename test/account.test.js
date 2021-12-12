const pactum = require('pactum');
const {int, expression} = require('pactum-matchers');

describe('Account operations', () => {
    let e2e = pactum.e2e("Account operations")

    it('Create account', async () => {
        await e2e.step('Post account')
            .spec('Create Account', {'@DATA:TEMPLATE@': 'Account:Expense'})
            .stores('AccountID', 'data.id')
            .expectJsonMatch({
                '@DATA:TEMPLATE@': 'Account:Expense', '@OVERRIDES@': {
                    data: {
                        id: int()
                    }
                }
            })
    })

    it('List accounts', async () => {
        await e2e.step('List accounts')
            .spec('read')
            .get('/account')
            .expectJsonMatch('data[*].id', expression('$S{AccountID}', '$V.includes($S{AccountID})'))
    })

    it('Read account', async () => {
        await e2e.step('Read account')
            .spec('read')
            .get("/account/{id}")
            .withPathParams('id', '$S{AccountID}')
            .expectJsonMatch({
                '@DATA:TEMPLATE@': 'Account:Expense', '@OVERRIDES@': {
                    data: {
                        id: int()
                    }
                }
            })
    })

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
                            name: "Monthly rent"
                        }
                    }
                }
            })
            .expectJson("data.attributes.name", "Monthly rent")

        await e2e.step('Read account')
            .spec('read')
            .get("/account/{id}")
            .withPathParams('id', '$S{AccountID}')
            .expectJson("data.attributes.name", "Monthly rent")

        await e2e.cleanup()
    })
})