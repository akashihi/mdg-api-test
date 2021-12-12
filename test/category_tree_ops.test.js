const pactum = require('pactum');
const {like, string, expression, int} = require('pactum-matchers');

async function make_tree() {
    const outerCategoryID = await pactum.spec('Create Category', {
        '@DATA:TEMPLATE@': 'Category:BasicCategory',
        '@OVERRIDES@': {
            data: {
                attributes: {
                    name: "outer"
                }
            }
        }
    })
        .returns('data.id');

    const middleCategoryID = await pactum.spec('Create Category', {
        '@DATA:TEMPLATE@': 'Category:BasicCategory',
        '@OVERRIDES@': {
            data: {
                attributes: {
                    parent_id: outerCategoryID,
                    name: "middle"
                }
            }
        }
    })
        .returns('data.id');

    const innerCategoryID = await pactum.spec('Create Category', {
        '@DATA:TEMPLATE@': 'Category:BasicCategory',
        '@OVERRIDES@': {
            data: {
                attributes: {
                    parent_id: middleCategoryID,
                    name: "inner"
                }
            }
        }
    })
        .returns('data.id');
    return {
        outer: outerCategoryID,
        middle: middleCategoryID,
        inner: innerCategoryID
    }
}

it('Category parenting', async () => {
    const categories = await make_tree();

    await pactum.spec('Get Category Tree', categories.outer)
        .expectJson('data.id', categories.outer)
        .expectJson('data.attributes.children[0].id', categories.middle)
        .expectJson('data.attributes.children[0].children[0].id', categories.inner)

});

it('Category re-parenting', async () => {
    const categories = await make_tree();

    await pactum.spec('update')
        .put('/category/{id}')
        .withPathParams('id', categories.inner)
        .withJson({
            '@DATA:TEMPLATE@': 'Category:BasicCategory',
            '@OVERRIDES@': {
                data: {
                    attributes: {
                        parent_id: categories.outer
                    }
                }
            }
        })
    //.expectJson("data.attributes.parent_id", outerCategoryID) //TODO backend bug

    await pactum.spec('Get Category Tree', categories.outer)
        .expectJsonLike('data.attributes.children[*].id', [categories.middle, categories.inner])

});

it('Category cyclic reparenting is prevented', async () => {
    const categories = await make_tree();

    await pactum.spec('expect error', {status_code: 412, error_code: 'CATEGORY_TREE_CYCLED'})
        .put('/category/{id}')
        .withPathParams('id', categories.outer)
        .withJson({
            '@DATA:TEMPLATE@': 'Category:BasicCategory',
            '@OVERRIDES@': {
                data: {
                    attributes: {
                        parent_id: categories.inner
                    }
                }
            }
        })
});

it('Self re-parenting move to top', async () => {
    const categories = await make_tree();

    await pactum.spec('update')
        .put('/category/{id}')
        .withPathParams('id', categories.middle)
        .withJson({
            '@DATA:TEMPLATE@': 'Category:BasicCategory',
            '@OVERRIDES@': {
                data: {
                    attributes: {
                        parent_id: categories.middle
                    }
                }
            }
        })
    //.expectJson("data.attributes.parent_id", null) //TODO backend bug

    await pactum.spec('Get Category Tree', categories.outer)
        .expectJsonMatch('data.attributes', expression('undefined', '!$V.hasOwnProperty("children")'))

    await pactum.spec('Get Category Tree', categories.middle)
        .expectJson('data.attributes.children[0].id', categories.inner)
});

it('Category of one type can not be parented to category of different type', async () => {
    const outerCategoryID = await pactum.spec('Create Category', {'@DATA:TEMPLATE@': 'Category:BasicCategory' })
        .returns('data.id');

    await pactum.spec('expect error', {status_code: 412, error_code: 'CATEGORY_INVALID_TYPE'})
        .post("/category")
        .withJson({
            '@DATA:TEMPLATE@': 'Category:BasicCategory',
            '@OVERRIDES@': {
                data: {
                    attributes: {
                        account_type: 'income',
                        parent_id: outerCategoryID
                    }
                }
            }
        })
});

it('Category of one type can not be re-parented to category of different type', async () => {
    const expenseCategoryID = await pactum.spec('Create Category', {'@DATA:TEMPLATE@': 'Category:BasicCategory'})
        .returns('data.id');

    const incomeCategoryID = await pactum.spec('Create Category', {
        '@DATA:TEMPLATE@': 'Category:BasicCategory',
        '@OVERRIDES@': {
            data: {
                attributes: {
                    account_type: 'income'
                }
            }
        }
    })
        .returns('data.id');

    await pactum.spec('expect error', {status_code: 412, error_code: 'CATEGORY_INVALID_TYPE'})
        .put('/category/{id}')
        .withPathParams('id', incomeCategoryID)
        .withJson({
            '@DATA:TEMPLATE@': 'Category:BasicCategory',
            '@OVERRIDES@': {
                data: {
                    attributes: {
                        parent_id: expenseCategoryID
                    }
                }
            }
        })
});