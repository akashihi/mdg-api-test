const pactum = require('pactum');
const { int, expression } = require('pactum-matchers');

describe('Account operations', () => {
    const e2e = pactum.e2e('Account operations');

    it('Create account', async () => {
        await e2e.step('Post account')
            .spec('Create Account', { '@DATA:TEMPLATE@': 'Account:Expense:V1' })
            .stores('AccountID', 'id')
            .expectJson("name", "Rent")
            .expectJson("account_type", "EXPENSE")
            .expectJsonMatch("id",int());
    });

    it('List accounts', async () => {
        await e2e.step('List accounts')
            .spec('read')
            .get('/accounts')
            .expectJsonMatch('accounts[*].id', expression('$S{AccountID}', '$V.includes($S{AccountID})'));
    });

    it('Read account', async () => {
        await e2e.step('Read account')
            .spec('read')
            .get('/accounts/{id}')
            .withPathParams('id', '$S{AccountID}')
            .expectJson("name", "Rent")
            .expectJson("account_type", "EXPENSE")
            .expectJson("currency_id", 978);
    });

    it('Read account with currency embedded', async () => {
        await e2e.step('Read account')
            .spec('read')
            .get('/accounts/{id}')
            .withPathParams('id', '$S{AccountID}')
            .withQueryParams({ embed: 'currency' })
            .expectJson("name", "Rent")
            .expectJson("account_type", "EXPENSE")
            .expectJson("currency_id", 978)
            .expectJson("currency.id", 978)
            .expectJson("currency.code", "EUR");
    });

    it('Update account', async () => {
        await e2e.step('Update account')
            .spec('update')
            .put('/accounts/{id}')
            .withPathParams('id', '$S{AccountID}')
            .withJson({
                '@DATA:TEMPLATE@': 'Account:Expense:V1',
                '@OVERRIDES@': {
                    name: 'Monthly rent'
                }
            })
            .expectJson('name', 'Monthly rent');

        await e2e.step('Read account')
            .spec('read')
            .get('/accounts/{id}')
            .withPathParams('id', '$S{AccountID}')
            .expectJson('name', 'Monthly rent');
    });

    it('Hide account', async () => {
        await e2e.step('Hide account')
            .spec('delete')
            .delete('/accounts/{id}')
            .withPathParams('id', '$S{AccountID}');
    });

    it('Hidden account is in non-filtered account lists', async () => {
        await e2e.step('Hidden account is in non-filtered account lists')
            .spec('read')
            .get('/accounts')
            .expectJsonMatch('accounts[*].id', expression('$S{AccountID}', '$V.includes($S{AccountID})'));
    });

    it('Hidden account is in hidden accounts list', async () => {
        await e2e.step('Hidden account is in hidden accounts list')
            .spec('read')
            .get('/accounts')
            .withQueryParams({ q: '%7B%22hidden%22%3A%22true%22%7D' })
            .expectJsonMatch('accounts[*].id', expression('$S{AccountID}', '$V.includes($S{AccountID})'));
    });

    it('Hidden account is not in active accounts list', async () => {
        await e2e.step('Hidden account is not in active accounts list')
            .spec('read')
            .get('/accounts')
            .withQueryParams({ q: "%7B%22hidden%22%3A%22false%22%7D" })
            .expectJsonMatch('accounts[*].id', expression('$S{AccountID}', '!$V.includes($S{AccountID})'));
    });

    it('Specific filtering ignores hidden flag', async () => {
        await e2e.step('Specific filtering ignores hidden flag')
            .spec('read')
            .get('/accounts')
            .withQueryParams({ q: '%7B%22name%22%3A%22Monthly%20rent%22%7D'})
            .expectJsonMatch('accounts[*].id', expression('$S{AccountID}', '$V.includes($S{AccountID})'));

        await e2e.cleanup();
    });
});
