const pactum = require('pactum');
const { createAccountForTransaction, createUSDAccountForTransaction } = require('./transaction.handler');

describe('Transaction multi-currency operations', () => {
    const e2e = pactum.e2e('Transaction multi-currency operations');

    it('Create multi-currency transaction', async () => {
        await createAccountForTransaction(e2e);
        await createUSDAccountForTransaction(e2e);

        await e2e.step('Create multi-currency transaction')
            .spec('Create Transaction', { '@DATA:TEMPLATE@': 'Transaction:MultiCurrency:V1' })
            .stores('TransactionID', 'id')
            .expectJsonLike('operations[*].amount', [-100, 200])
            .expectJsonLike('operations[*].rate', [0.5]);
    });

    it('Read multi-currency transaction', async () => {
        await e2e.step('Read transaction')
            .spec('read')
            .get('/transactions/{id}')
            .withPathParams('id', '$S{TransactionID}')
            .expectJsonLike('operations[*].amount', [-100, 200])
            .expectJsonLike('operations[*].rate', [0.5]);

        await e2e.cleanup();
    });
});
