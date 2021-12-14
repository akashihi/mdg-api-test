const pactum = require('pactum');
const {expression} = require('pactum-matchers');
const {createAccountForTransaction} = require('./transaction.handler')

describe('Transaction filtering', () => {
    let e2e = pactum.e2e("Transaction operations")

    it('Create multiple transactions', async () => {
        await createAccountForTransaction(e2e)

        await e2e.step('Create transaction')
            .spec('Create Transaction', {
                '@DATA:TEMPLATE@': 'Transaction:Rent',
                '@OVERRIDES@': {
                    data: {
                        attributes: {
                            timestamp: '2017-02-05T16:45:36'
                        }
                    }
                }
            })

        await e2e.step('Create transaction')
            .spec('Create Transaction', {
                '@DATA:TEMPLATE@': 'Transaction:Rent',
                '@OVERRIDES@': {
                    data: {
                        attributes: {
                            timestamp: '2017-02-06T16:45:36'
                        }
                    }
                }
            })
        await e2e.step('Create transaction')
            .spec('Create Transaction', {'@DATA:TEMPLATE@': 'Transaction:Rent'})
    })

    it('Transaction timestamp descending sort', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transaction')
            .withQueryParams('sort', 'timestamp')
            .expect((ctx) => {
                const data = ctx.res.json.data;
                let current_ts = Date.parse(data[0].attributes.timestamp);
                for (let tx of data) {
                    if (!(Date.parse(tx.attributes.timestamp) <= current_ts)) {
                        throw "Transaction order incorrect"
                    }
                    current_ts = Date.parse(tx.attributes.timestamp)
                }
            })
    })

    it('Transaction timestamp ascending sort', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transaction')
            .withQueryParams('sort', '-timestamp')
            .expect((ctx) => {
                const data = ctx.res.json.data;
                let current_ts = Date.parse(data[0].attributes.timestamp);
                for (let tx of data) {
                    if (!(Date.parse(tx.attributes.timestamp) >= current_ts)) {
                        throw "Transaction order incorrect"
                    }
                    current_ts = Date.parse(tx.attributes.timestamp)
                }
            })
    })

    it('Transaction timestamp filtering using notEarlier', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transaction')
            .withQueryParams('notEarlier', '2017-02-06T00:00:00')
            .expect((ctx) => {
                const data = ctx.res.json.data;
                for (let tx of data) {
                    if (!(Date.parse(tx.attributes.timestamp) >= Date.parse('2017-02-06T00:00:00'))) {
                        throw "Transaction order incorrect"
                    }
                }
            })
    })

    it('Transaction timestamp filtering using notLater', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transaction')
            .withQueryParams('notLater', '2017-02-05T00:00:00')
            .expect((ctx) => {
                const data = ctx.res.json.data;
                for (let tx of data) {
                    if (!(Date.parse(tx.attributes.timestamp) <= Date.parse('2017-02-04T23:59:59'))) {
                        throw "Transaction order incorrect"
                    }
                }
            })
    })

    it('Transaction timestamp filtering for date range', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transaction')
            .withQueryParams('notEarlier', '2017-02-05T00:00:00')
            .withQueryParams('notLater', '2017-02-06T00:00:00')
            .expect((ctx) => {
                const data = ctx.res.json.data;
                for (let tx of data) {
                    if (!(Date.parse(tx.attributes.timestamp) >= Date.parse('2017-02-05T00:00:00') && Date.parse(tx.attributes.timestamp) <= Date.parse('2017-02-05T23:59:59'))) {
                        throw "Transaction order incorrect"
                    }
                }
            })
    })

    it('Transaction filtering on account', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transaction')
            .withQueryParams("filter", '{"account_id": [$S{AssetAccountID}]}')
            .expectJsonMatch('data', expression('1', '$V.length === 3')) //We use new account ids and create 3 transactions with the same account

        await e2e.cleanup()
    })
})
