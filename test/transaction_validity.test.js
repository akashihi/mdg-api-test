const pactum = require("pactum");
const {createAccountForTransaction} = require('./transaction.handler')

it('Empty transactions are not allowed', async () => {
    await createAccountForTransaction()

    //No way to remove field from the template
    await pactum.spec('expect error', {status_code: 412, error_code: 'TRANSACTION_EMPTY'})
        .post('/transaction')
        .withJson( {
            "data": {
                "attributes": {
                    "timestamp": "2017-02-04T16:45:36",
                    "comment": "Test transaction",
                    "tags": [
                        "test",
                        "transaction"
                    ],
                    "operations": []
                },
                "type": "transaction"
            }
        })

});

it('Empty operations are ignored', async () => {
    await createAccountForTransaction()

    await pactum.spec('expect error', {status_code: 412, error_code: 'TRANSACTION_EMPTY'})
        .post('/transaction')
        .withJson( {
            '@DATA:TEMPLATE@': 'Transaction:Rent',
            '@OVERRIDES@': {
                data: {
                    attributes: {
                        operations: [
                            {
                                amount: 0
                            },
                            {
                                amount: 0
                            },
                            {
                                amount: 0
                            }
                        ]
                    }
                }
            }
        })

});

it('Unbalanced transactions are not allowed', async () => {
    await createAccountForTransaction()

    await pactum.spec('expect error', {status_code: 412, error_code: 'TRANSACTION_NOT_BALANCED'})
        .post('/transaction')
        .withJson( {
            '@DATA:TEMPLATE@': 'Transaction:Rent',
            '@OVERRIDES@': {
                data: {
                    attributes: {
                        operations: [
                            {
                                amount: -50
                            },
                            {
                                amount: 100
                            },
                            {
                                amount: 25
                            }
                        ]
                    }
                }
            }
        })

});