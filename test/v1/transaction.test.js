const pactum = require('pactum');
const { addCaptureHandler } = require('pactum').handler;
const { expression } = require('pactum-matchers');
const { createAccountForTransaction, checkAccountsBalances } = require('./transaction.handler');

before(() => {
    addCaptureHandler('GetTransactionCount', (ctx) => {
        const res = ctx.res;
        return res.json.transactions.length;
    });
});

describe('Transaction operations', () => {
    const e2e = pactum.e2e('Transaction operations');

    it('Get transactions count', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transactions')
            .stores('TransactionCount', '#GetTransactionCount');
    });

    it('Create Transaction', async () => {
        await createAccountForTransaction(e2e);

        await e2e.step('Create transaction')
            .spec('Create Transaction', { '@DATA:TEMPLATE@': 'Transaction:Rent:V1' })
            .stores('TransactionID', 'id')
            .expectJsonLike({'@DATA:TEMPLATE@': 'Transaction:Rent:V1'});
    });

    it('Transaction count is increased after creation', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transactions')
            .expectJsonMatch('transactions', expression('$S{TransactionCount} + 1', '$V.length === $S{TransactionCount} + 1'));
    });

    it('List transactions', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transactions')
            .expectJsonMatch('transactions[*].id', expression('$S{TransactionID}', '$V.includes($S{TransactionID})'));
    });

    it('Read transaction', async () => {
        await e2e.step('Read transaction')
            .spec('read')
            .get('/transactions/{id}')
            .withPathParams('id', '$S{TransactionID}')
            .expectJson("id", '$S{TransactionID}')
            .expectJsonLike({'@DATA:TEMPLATE@': 'Transaction:Rent:V1'});
    });

    it('Newly created transaction updates accounts balances', async () => {
        await checkAccountsBalances(e2e, -150, 50, 100);
    });

    it('Update transaction', async () => {
        await e2e.step('Update transaction')
            .spec('update')
            .put('/transactions/{id}')
            .withPathParams('id', '$S{TransactionID}')
            .withJson({
                '@DATA:TEMPLATE@': 'Transaction:Rent:V1',
                '@OVERRIDES@': {
                    operations: [
                        {
                        },
                        {
                            amount: 80
                        },
                        {
                            amount: 70
                        }
                    ]
                }
            })
            .expectJsonLike('operations[*].amount', [-150, 80, 70]);

        await e2e.step('Read updated transaction')
            .spec('read')
            .get('/transactions/{id}')
            .withPathParams('id', '$S{TransactionID}')
            .expectJsonLike('operations[*].amount', [-150, 80, 70]);
    });

    it('Transaction count is untouched after update', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transactions')
            .expectJsonMatch('transactions', expression('$S{TransactionCount} + 1', '$V.length === $S{TransactionCount} + 1'));
    });

    it('Updated transaction updates accounts balances', async () => {
        await checkAccountsBalances(e2e, -150, 80, 70);
    });

    it('Delete transaction', async () => {
        await e2e.step('Delete transaction')
            .spec('delete')
            .delete('/transactions/{id}')
            .withPathParams('id', '$S{TransactionID}');

        await e2e.step('List transactions')
            .spec('read')
            .get('/transactions')
            .expectJsonMatch('transactions[*].id', expression('$S{TransactionID}', '!$V.includes($S{TransactionID})'));
    });

    it('Transaction count is reverted after deletion', async () => {
        await e2e.step('List transactions')
            .spec('read')
            .get('/transactions')
            .expectJsonMatch('transactions', expression('$S{TransactionCount}', '$V.length === $S{TransactionCount}'));
    });

    it('Transaction deletion reverts accounts balances', async () => {
        await checkAccountsBalances(e2e, 0, 0, 0);

        await e2e.cleanup();
    });
});
