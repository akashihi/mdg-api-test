const pactum = require('pactum')

describe('Category-Account operations', () => {
    let e2e = pactum.e2e("Category-Account operations")

    it('Create category', async () => {
        await e2e.step('Post category')
            .spec('Create Category', {'@DATA:TEMPLATE@': 'Category:BasicCategory'})
            .stores('CategoryID', 'data.id')
    })

    it('Create account with category', async () => {
        await e2e.step('Create account with category')
            .spec('Create Account', {
                '@DATA:TEMPLATE@': 'Account:Expense',
                '@OVERRIDES@': {
                    data: {
                        attributes: {
                            category_id: '$S{CategoryID}'
                        }
                    }
                }
            })
            .expectJson("data.attributes.category_id", '$S{CategoryID}')
            .stores('AccountID', 'data.id')
    })

    it('Read account with category', async () => {
        await e2e.step('Read account with category')
        await pactum.spec('read')
            .get("/account/{id}")
            .withPathParams('id', '$S{AccountID}')
            .expectJson("data.attributes.category_id", '$S{CategoryID}')
    })

    it('Delete category', async () => {
        await e2e.step('Delete category')
            .spec('delete')
            .delete('/category/{id}')
            .withPathParams('id', '$S{CategoryID}');

        await pactum.spec('read')
            .get("/account/{id}")
            .withPathParams('id', '$S{AccountID}')
            .expectJson("data.attributes.category_id", null)

    })


    it('Create one more category', async () => {
        await e2e.step('Post category')
            .spec('Create Category', {'@DATA:TEMPLATE@': 'Category:BasicCategory'})
            .stores('CategoryID', 'data.id')
    })

    it('Assign account to the new category', async () => {
        await e2e.step('Post category')
            .spec('update')
            .put('/account/{id}')
            .withPathParams('id', '$S{AccountID}')
            .withJson({
                '@DATA:TEMPLATE@': 'Account:Expense',
                '@OVERRIDES@': {
                    data: {
                        attributes: {
                            category_id: '$S{CategoryID}'
                        }
                    }
                }
            })
            .expectJson("data.attributes.category_id", '$S{CategoryID}')

        await pactum.spec('read')
            .get("/account/{id}")
            .withPathParams('id', '$S{AccountID}')
            .expectJson("data.attributes.category_id", '$S{CategoryID}')

        await e2e.cleanup()
    })
})

it('Asset account has default category', async () => {
    const category_response = await pactum.spec('read')
        .get('/category')
    const categories = category_response.json
    const categoryID = categories.data.filter((c) => c.attributes.name === "Current").map((c) => c.id)[0]

    const accountID = await pactum.spec('Create Account', {'@DATA:TEMPLATE@': 'Account:Asset'})
        .expectJson("data.attributes.category_id", categoryID)
        .returns("data.id")

    await pactum.spec('read')
        .get("/account/{id}")
        .withPathParams('id', accountID)
        .expectJson("data.attributes.category_id", categoryID)
})

it('Category can not be assigned to the incompatible account', async () => {
    const categoryID = await pactum.spec('Create Category', {'@DATA:TEMPLATE@': 'Category:BasicCategory'})
        .returns("data.id")

    const accountID = await pactum.spec('Create Account', {'@DATA:TEMPLATE@': 'Account:Asset'})
        .returns("data.id")

    await pactum.spec('expect error', {status_code: 412, error_code: "CATEGORY_INVALID_TYPE"})
        .put('/account/{id}')
        .withPathParams('id', accountID)
        .withJson({
            '@DATA:TEMPLATE@': 'Account:Asset',
            '@OVERRIDES@': {
                data: {
                    attributes: {
                        category_id: categoryID
                    }
                }
            }
        });
})