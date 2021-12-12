const pactum = require('pactum');
const {int, expression} = require('pactum-matchers');

describe('Account flag management', () => {
    let e2e = pactum.e2e("Account operations")

    it('Create account with flags', async () => {
        await e2e.step('Post account')
            .spec('Create Account', {'@DATA:TEMPLATE@': 'Account:Asset'})
            .stores('AccountID', 'data.id')
            .expectJson("data.attributes.favorite", true)
            .expectJson("data.attributes.operational", true)
    })

    it('List flagged accounts', async () => {
        await e2e.step('List accounts')
            .spec('read')
            .get('/account')
            .expectJson('data[id=$S{AccountID}].attributes.favorite', true)
            .expectJson('data[id=$S{AccountID}].attributes.operational', true)
    })

    it('Read account with flags', async () => {
        await e2e.step('Read account')
            .spec('read')
            .get("/account/{id}")
            .withPathParams('id', '$S{AccountID}')
            .expectJson("data.attributes.favorite", true)
            .expectJson("data.attributes.operational", true)
    })

    it('Update flagged account', async () => {
        await e2e.step('Update flagged account')
            .spec('update')
            .put('/account/{id}')
            .withPathParams('id', '$S{AccountID}')
            .withJson({
                '@DATA:TEMPLATE@': 'Account:Asset',
                '@OVERRIDES@': {
                    data: {
                        id: '$S{AccountID}',
                        attributes: {
                            operational: false,
                            favorite: false
                        }
                    }
                }
            })
            .expectJson("data.attributes.favorite", false)
            .expectJson("data.attributes.operational", false)

        await e2e.step('Read flagged account')
            .spec('read')
            .get("/account/{id}")
            .withPathParams('id', '$S{AccountID}')
            .expectJson("data.attributes.favorite", false)
            .expectJson("data.attributes.operational", false)
        await e2e.cleanup()
    })
})