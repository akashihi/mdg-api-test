const pactum = require('pactum');
const { expression } = require('pactum-matchers');
const { createAccountForTransaction } = require('./transaction.handler');

describe('Transaction filtering', () => {
    const e2e = pactum.e2e('Transaction filtering');

    it('Create multiple transactions', async () => {
        await createAccountForTransaction(e2e);

        await e2e.step('Create transaction')
            .spec('Create Transaction', {
                '@DATA:TEMPLATE@': 'Transaction:Rent:V1',
                '@OVERRIDES@': {
                    timestamp: '2017-02-05T16:45:36'
                }
            });

        await e2e.step('Create transaction')
            .spec('Create Transaction', {
                '@DATA:TEMPLATE@': 'Transaction:Rent:V1',
                '@OVERRIDES@': {
                    timestamp: '2017-02-06T16:45:36'
                }
            });
        await e2e.step('Create transaction')
            .spec('Create Transaction', { '@DATA:TEMPLATE@': 'Transaction:Rent:V1' });
    });

    it('Transaction timestamp descending sort', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transactions')
            .withQueryParams('sort', 'timestamp')
            .expect((ctx) => {
                const data = ctx.res.json.transactions;
                let currentTs = Date.parse(data[0].timestamp);
                for (const tx of data) {
                    if (Date.parse(tx.timestamp) > currentTs) {
                        throw new Error('Transaction order incorrect');
                    }
                    currentTs = Date.parse(tx.timestamp);
                }
            });
    });

    it('Transaction timestamp ascending sort', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transactions')
            .withQueryParams('sort', '-timestamp')
            .expect((ctx) => {
                const data = ctx.res.json.transactions;
                let currentTs = Date.parse(data[0].timestamp);
                for (const tx of data) {
                    if (Date.parse(tx.timestamp) < currentTs) {
                        throw new Error('Transaction order incorrect');
                    }
                    currentTs = Date.parse(tx.timestamp);
                }
            });
    });

    it('Transaction timestamp filtering using notEarlier', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transactions')
            .withQueryParams('q', "%7B%22notEarlier%22%3A%20%222017-02-06T00%3A00%3A00%22%7D")
            .expect((ctx) => {
                const data = ctx.res.json.transactions;
                for (const tx of data) {
                    if (Date.parse(tx.timestamp) < Date.parse('2017-02-06T00:00:00')) {
                        throw new Error('Transaction order incorrect');
                    }
                }
            });
    });

    it('Transaction timestamp filtering using notLater', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transactions')
            .withQueryParams('q', "%7B%22notLater%22%3A%20%222017-02-05T00%3A00%3A00%22%7D")
            .expect((ctx) => {
                const data = ctx.res.json.transactions;
                for (const tx of data) {
                    if (Date.parse(tx.timestamp) > Date.parse('2017-02-04T23:59:59')) {
                        throw new Error('Transaction order incorrect');
                    }
                }
            });
    });

    it('Transaction timestamp filtering for date range', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transactions')
            .withQueryParams('q', "%7B%22notLater%22%3A%20%222017-02-06T00%3A00%3A00%22%2C%20%22notEarlier%22%3A%20%222017-02-05T00%3A00%3A00%22%7D")
            .expect((ctx) => {
                const data = ctx.res.json.transactions;
                for (const tx of data) {
                    if (!(Date.parse(tx.timestamp) >= Date.parse('2017-02-05T00:00:00') && Date.parse(tx.timestamp) <= Date.parse('2017-02-05T23:59:59'))) {
                        throw new Error('Transaction order incorrect');
                    }
                }
            });
    });

    it('Transaction filtering on account', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transactions')
            .withQueryParams('q', '%7B%22account_id%22%3A%20%22%5B$S{AssetAccountID}%5D%22%7D')
            .expectJsonMatch('transactions', expression('1', '$V.length === 3')); // We use new account ids and create 3 transactions with the same account

        await e2e.cleanup();
    });
});
