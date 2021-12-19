const pactum = require('pactum');
const {createAccountForTransaction} = require('./transaction.handler')

describe('Transaction full text search', () => {
    let e2e = pactum.e2e("Transaction operations")

    it("Force transactions full text re-index", async () => {
        await e2e.step("Force transactions full text re-index")
            .spec("update")
            .put('/setting/{id}')
            .withPathParams('id', "mnt.transaction.reindex")
            .withRequestTimeout(10000)
            .expectResponseTime(10000)
    }).timeout(15000)

    it('Create income transactions', async () => {
        await createAccountForTransaction(e2e)

        await e2e.step('Create transaction')
            .spec('Create Transaction', {
                '@DATA:TEMPLATE@': 'Transaction:Income'})
    })

    it('Transaction search by malformed comment', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transaction')
            .withQueryParams("filter", '{"comment": "incme"}')
            .expectJsonLike('data[*].attributes.comment', ['Income transaction']);

        await e2e.cleanup()
    })

    it('Transaction search by malformed tag', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transaction')
            .withQueryParams("filter", '{"tag": "incme"}')
            .expectJsonLike('data[*].attributes.tags', ['income', 'transaction']);

        await e2e.cleanup()
    })
})
