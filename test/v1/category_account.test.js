const pactum = require('pactum');

describe('Category-Account operations', () => {
    const e2e = pactum.e2e('Category-Account operations');

    it('Create category', async () => {
        await e2e.step('Post category')
            .spec('Create Category', { '@DATA:TEMPLATE@': 'Category:Basic:V1' })
            .stores('CategoryID', 'id')
            .clean()
            .delete('/categories/{id}')
            .withPathParams('id', '$S{CategoryID}');
    });

    it('Create account with category', async () => {
        await e2e.step('Create account with category')
            .spec('Create Account', {
                '@DATA:TEMPLATE@': 'Account:Expense:V1',
                '@OVERRIDES@': {
                    category_id: '$S{CategoryID}'
                }
            })
            .expectJson('category_id', '$S{CategoryID}')
            .stores('AccountID', 'id');
    });

    it('Read account with category', async () => {
        await e2e.step('Read account with category');
        await pactum.spec('read')
            .get('/accounts/{id}')
            .withPathParams('id', '$S{AccountID}')
            .expectJson('category_id', '$S{CategoryID}');
    });

    it('Read account with category object embedded', async () => {
        await e2e.step('Read account with category');
        await pactum.spec('read')
            .get('/accounts/{id}')
            .withPathParams('id', '$S{AccountID}')
            .withQueryParams({ embed: 'currency,category' })
            .expectJson("currency.id", 978)
            .expectJson("currency.code", "EUR")
            .expectJson('category_id', '$S{CategoryID}')
            .expectJson("category.id", '$S{CategoryID}')
            .expectJson("category.name", "Bonuses");
    });

    it('Delete category', async () => {
        await e2e.step('Delete category')
            .spec('delete')
            .delete('/categories/{id}')
            .withPathParams('id', '$S{CategoryID}');

        await pactum.spec('read')
            .get('/accounts/{id}')
            .withPathParams('id', '$S{AccountID}')
            .withQueryParams({ embed: 'category' })
            .expectJson('category_id', null);
    });

    it('Create one more category', async () => {
        await e2e.step('Post category')
            .spec('Create Category', { '@DATA:TEMPLATE@': 'Category:Basic:V1' })
            .stores('CategoryID', 'id');
    });

    it('Assign account to the new category', async () => {
        await e2e.step('Post category')
            .spec('update')
            .put('/accounts/{id}')
            .withPathParams('id', '$S{AccountID}')
            .withJson({
                '@DATA:TEMPLATE@': 'Account:Expense:V1',
                '@OVERRIDES@': {
                    category_id: '$S{CategoryID}'
                }
            })
            .expectJson('category_id', '$S{CategoryID}');

        await pactum.spec('read')
            .get('/accounts/{id}')
            .withPathParams('id', '$S{AccountID}')
            .expectJson('category_id', '$S{CategoryID}');

        await e2e.cleanup();
    });
});

it('Asset account has default category', async () => {
    const categoryResponse = await pactum.spec('read')
        .get('/categories');
    const categories = categoryResponse.json;
    const categoryID = categories.categories.filter((c) => c.name === 'Current').map((c) => c.id)[0];

    const accountID = await pactum.spec('Create Account', { '@DATA:TEMPLATE@': 'Account:Asset:V1' })
        .expectJson('category_id', categoryID)
        .returns('id');

    await pactum.spec('read')
        .get('/accounts/{id}')
        .withPathParams('id', accountID)
        .expectJson('category_id', categoryID);
});

it('Category can not be assigned to the incompatible account', async () => {
    const categoryID = await pactum.spec('Create Category', { '@DATA:TEMPLATE@': 'Category:Basic:V1' })
        .returns('id');

    const accountID = await pactum.spec('Create Account', { '@DATA:TEMPLATE@': 'Account:Asset:V1' })
        .returns('id');

    await pactum.spec('expect error', { statusCode: 412, title: 'CATEGORY_INVALID_TYPE' })
        .put('/accounts/{id}')
        .withPathParams('id', accountID)
        .withJson({
            '@DATA:TEMPLATE@': 'Account:Asset:V1',
            '@OVERRIDES@': {
                category_id: categoryID
            }
        });
});
