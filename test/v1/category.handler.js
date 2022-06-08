const pactum = require('pactum');

pactum.handler.addSpecHandler('Create Category', (ctx) => {
    const { spec, data } = ctx;
    spec.post('/categories');
    spec.withJson(data);
    spec.use('create');
});

pactum.handler.addSpecHandler('Get Category Tree', (ctx) => {
    const { spec, data } = ctx;
    spec.get('/categories/{id}');
    spec.withPathParams('id', data);
    spec.use('read');
});

async function makeTree() {
    const outerCategoryID = await pactum.spec('Create Category', {
        '@DATA:TEMPLATE@': 'Category:Basic:V1',
        '@OVERRIDES@': {
            name: 'outer'
        }
    })
        .returns('id');

    const middleCategoryID = await pactum.spec('Create Category', {
        '@DATA:TEMPLATE@': 'Category:Basic:V1',
        '@OVERRIDES@': {
            parent_id: outerCategoryID,
            name: 'middle'
        }
    })
        .returns('id');

    const innerCategoryID = await pactum.spec('Create Category', {
        '@DATA:TEMPLATE@': 'Category:Basic:V1',
        '@OVERRIDES@': {
            parent_id: middleCategoryID,
            name: 'inner'
        }
    })
        .returns('id');
    return {
        outer: outerCategoryID,
        middle: middleCategoryID,
        inner: innerCategoryID
    };
}

async function dropTree(tree) {
    await pactum.spec().delete('/categories/{id}').withPathParams("id", tree.outer);
    await pactum.spec().delete('/categories/{id}').withPathParams("id", tree.middle);
    await pactum.spec().delete('/categories/{id}').withPathParams("id", tree.inner);
}

module.exports = {makeTree, dropTree};
