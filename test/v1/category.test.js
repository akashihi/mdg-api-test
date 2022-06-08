const pactum = require('pactum');
const { int, expression } = require('pactum-matchers');

describe('Category operations', () => {
    const e2e = pactum.e2e('Category operations');

    it('Create category', async () => {
        await e2e.step('Post category')
            .spec('Create Category', { '@DATA:TEMPLATE@': 'Category:Basic:V1' })
            .stores('CategoryID', 'id')
            .expectJsonMatch({id: int()})
            .expectJsonLike({ '@DATA:TEMPLATE@': 'Category:Basic:V1' });
    });

    it('List categories', async () => {
        await e2e.step('List categories')
            .spec('read')
            .get('/categories')
            .expectJsonMatch('categories[*].id', expression('$S{CategoryID}', '$V.includes($S{CategoryID})'));
    });

    it('Read category', async () => {
        await e2e.step('Read category')
            .spec('Get Category Tree', '$S{CategoryID}')
            .expectJsonMatch({id: int()})
            .expectJsonLike({ '@DATA:TEMPLATE@': 'Category:Basic:V1' });
    });

    it('Update category', async () => {
        await e2e.step('Update category')
            .spec('update')
            .put('/categories/{id}')
            .withPathParams('id', '$S{CategoryID}')
            .withJson({
                '@DATA:TEMPLATE@': 'Category:Basic:V1',
                '@OVERRIDES@': {
                    name: 'Salary'
                }
            })
            .expectJson('name', 'Salary');

        await e2e.step('Read updated category')
            .spec('Get Category Tree', '$S{CategoryID}')
            .expectJson('name', 'Salary');
    });

    it('Delete category', async () => {
        await e2e.step('Delete category')
            .spec('delete')
            .delete('/categories/{id}')
            .withPathParams('id', '$S{CategoryID}');

        await e2e.step('List categories')
            .spec('read')
            .get('/categories')
            .expectJsonMatch('categories[*].id', expression('$S{CategoryID}', '!$V.includes($S{CategoryID})'));

        await e2e.cleanup();
    });
});
