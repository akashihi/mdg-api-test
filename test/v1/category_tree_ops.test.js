const pactum = require('pactum');
const {makeTree, dropTree} = require('./category.handler')

it('Category parenting', async () => {
    const categories = await makeTree();

    await pactum.spec('Get Category Tree', categories.outer)
        .expectJson('id', categories.outer)
        .expectJson('children[0].id', categories.middle)
        .expectJson('children[0].children[0].id', categories.inner);

    await dropTree(categories);
});

it('Category re-parenting', async () => {
    const categories = await makeTree();

    await pactum.spec('update')
        .put('/categories/{id}')
        .withPathParams('id', categories.inner)
        .withJson({
            '@DATA:TEMPLATE@': 'Category:Basic:V1',
            '@OVERRIDES@': {
                parent_id: categories.outer
            }
        })
        .expectJson("parent_id", categories.outer);

    await pactum.spec('Get Category Tree', categories.outer)
        .expectJsonLike('children[*].id', [categories.middle, categories.inner]);

    await dropTree(categories);
});

it('Category cyclic reparenting is prevented', async () => {
    const categories = await makeTree();

    await pactum.spec('expect error', {statusCode: 412, title: 'CATEGORY_TREE_CYCLED'})
        .put('/categories/{id}')
        .withPathParams('id', categories.outer)
        .withJson({
            '@DATA:TEMPLATE@': 'Category:Basic:V1',
            '@OVERRIDES@': {
                parent_id: categories.inner
            }
        });

    await dropTree(categories);
});

it('Self re-parenting move to top', async () => {
    const categories = await makeTree();

    await pactum.spec('update')
        .put('/categories/{id}')
        .withPathParams('id', categories.middle)
        .withJson({
            '@DATA:TEMPLATE@': 'Category:Basic:V1',
            '@OVERRIDES@': {
                parent_id: categories.middle
            }
        })
        .expectJson("data.attributes.parent_id", null);

    await pactum.spec('Get Category Tree', categories.outer)
        .expectJson('children', []);

    await pactum.spec('Get Category Tree', categories.middle)
        .expectJson('children[0].id', categories.inner);

    await dropTree(categories);
});

it('Category of one type can not be parented to category of different type', async () => {
    const outerCategoryID = await pactum.spec('Create Category', {'@DATA:TEMPLATE@': 'Category:Basic:V1'})
        .returns('id');

    await pactum.spec('expect error', {statusCode: 412, title: 'CATEGORY_INVALID_TYPE'})
        .post('/categories')
        .withJson({
            '@DATA:TEMPLATE@': 'Category:Basic:V1',
            '@OVERRIDES@': {
                account_type: 'income',
                parent_id: outerCategoryID
            }
        });

    await pactum.spec().delete('/categories/{id}').withPathParams("id", outerCategoryID);
});

it('Category of one type can not be re-parented to category of different type', async () => {
    const expenseCategoryID = await pactum.spec('Create Category', {'@DATA:TEMPLATE@': 'Category:Basic:V1'})
        .returns('id');

    const incomeCategoryID = await pactum.spec('Create Category', {
        '@DATA:TEMPLATE@': 'Category:Basic:V1',
        '@OVERRIDES@': {
            account_type: 'income'
        }
    })
        .returns('id');

    await pactum.spec('expect error', {statusCode: 412, title: 'CATEGORY_INVALID_TYPE'})
        .put('/categories/{id}')
        .withPathParams('id', incomeCategoryID)
        .withJson({
            '@DATA:TEMPLATE@': 'Category:Basic:V1',
            '@OVERRIDES@': {
                parent_id: expenseCategoryID
            }
        });

    await pactum.spec().delete('/categories/{id}').withPathParams("id", expenseCategoryID);
    await pactum.spec().delete('/categories/{id}').withPathParams("id", incomeCategoryID);
});
