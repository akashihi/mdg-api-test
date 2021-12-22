const pactum = require('pactum');
var itParam = require('mocha-param');

const ACCOUNT_FLAGS = ['favorite', 'operational']

itParam("Can't create non-asset account with ${value} flag set", ACCOUNT_FLAGS, async (params) => {
    let attributes = {}
    attributes[params] = true;
    await pactum.spec('expect error', {status_code: 412, error_code: 'ACCOUNT_NONASSET_INVALIDFLAG'})
        .post("/account")
        .withJson({
            '@DATA:TEMPLATE@': 'Account:Expense',
            '@OVERRIDES@': {
                data: {
                    attributes: attributes
                }
            }
        });
})

itParam("Can't set ${value} flag on non-asset account", ACCOUNT_FLAGS, async (params) => {
    const accountID = await pactum.spec('Create Account', {'@DATA:TEMPLATE@': 'Account:Expense'})
        .returns("data.id")

    let attributes = {}
    attributes[params] = true;
    pactum.spec('expect error', {status_code: 412, error_code: 'ACCOUNT_NONASSET_INVALIDFLAG'})
        .put("/account/{id}")
        .withPathParams('id', accountID)
        .withJson({
            '@DATA:TEMPLATE@': 'Account:Expense',
            '@OVERRIDES@': {
                data: {
                    attributes: attributes
                }
            }
        })
})