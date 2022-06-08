const pactum = require('pactum');
const {makeTree, dropTree} = require('./category.handler');
const {stash} = require("pactum");

describe('Category-Account tree', () => {
    const e2e = pactum.e2e('Category-Account tree');
    let categories;
    it('Create categories tree', async () => {
        categories = await makeTree();
    });

    it('Create account with category', async () => {
        await e2e.step('Create account with category')
            .spec('Create Account', {
                '@DATA:TEMPLATE@': 'Account:Expense:V1',
                '@OVERRIDES@': {
                    category_id: categories.inner
                }
            })
            .stores('AccountID', 'id')
            .expectJson("category_id", categories.inner);
    });

    it('Read account within tree', async () => {
        await e2e.step('Read account with category');
        await pactum.spec('read')
            .get('/accounts/tree')
            .withPathParams('id', '$S{AccountID}')
            .expect((ctx) => {
                const dataStore = stash.getDataStore();
                let data = ctx.res.json;
                let expenseTree = data.expense;
                let outerTree = expenseTree.categories.filter(c => c.id === categories.outer)[0];
                if (outerTree.categories[0].categories[0].accounts[0].id !== dataStore.AccountID) {
                    throw new Error('Invalid sub-account ' + outerTree.categories[0].categories[0].accounts[0].id + ' expected ' + dataStore.AccountID);
                }
            });
    });

    it('Read account within tree with embedded objects', async () => {
        await e2e.step('Read account with category');
        await pactum.spec('read')
            .get('/accounts/tree')
            .withQueryParams({ embed: 'currency,category' })
            .expect((ctx) => {
                const dataStore = stash.getDataStore();
                let data = ctx.res.json;
                let expenseTree = data.expense;
                let outerTree = expenseTree.categories.filter(c => c.id === categories.outer)[0];
                if (outerTree.categories[0].categories[0].accounts[0].id !== dataStore.AccountID) {
                    throw new Error('Invalid sub-account ' + outerTree.categories[0].categories[0].accounts[0].id + ' expected ' + dataStore.AccountID);
                }
                if (outerTree.categories[0].categories[0].accounts[0].currency.id !== 978) {
                    throw new Error('Invalid sub-account currency' + outerTree.categories[0].categories[0].accounts[0].currency.id + ' expected 978');
                }
                if (outerTree.categories[0].categories[0].accounts[0].category.id !== categories.inner) {
                    throw new Error('Invalid sub-account category' + outerTree.categories[0].categories[0].accounts[0].category.id + ' expected ' + categories.inner);
                }
            });
    });

    it('Read account tree without account', async () => {
        await e2e.step('Read account with category');
        await pactum.spec('read')
            .get('/accounts/tree')
            .withQueryParams({ q: '%7B%22name%22%3A%22wallet%22%7D' })
            .expectJson('expense.categories', []);
    });

    it('Delete categories', async () => {
        await dropTree(categories);
        await e2e.cleanup();
    });

});