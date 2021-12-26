const pactum = require('pactum');
const { createAccountForTransaction } = require('./transaction.handler');

it.skip('Transaction with same currency is rebalanced precisely on currency change', async () => {
  await createAccountForTransaction();
  const usdAccountId = await pactum.spec('Create Account', { '@DATA:TEMPLATE@': 'Account:ExpenseUSD' })
    .stores('AssetUSDAccountID', 'data.id')
    .returns('data.id');

  const transactionId = await pactum.spec('Create Transaction', { '@DATA:TEMPLATE@': 'Transaction:MultiCurrency' })
    .returns('data.id');

  await pactum.spec('update')
    .put('/account/{id}')
    .withPathParams('id', usdAccountId)
    .withJson({
      '@DATA:TEMPLATE@': 'Account:Asset',
      '@OVERRIDES@': {
        data: {
          attributes: {
            currency_id: 978
          }
        }
      }
    })
    .expectJson('data.attributes.currency_id', 978)
    .expectJson('data.attributes.balance', 100);

  await pactum.spec('read')
    .get('/transaction/{id}')
    .withPathParams('id', transactionId)
    .expectJsonLike('data.attributes.operations[*].rate', [1, 1]);
}).timeout(15000);

it.skip('Transaction with default currency is rebalanced correctly on currency change', async () => {
  const accountId = await pactum.spec('Create Account', { '@DATA:TEMPLATE@': 'Account:Expense' })
    .stores('AssetAccountID', 'data.id')
    .returns('data.id');
  await pactum.spec('Create Account', { '@DATA:TEMPLATE@': 'Account:ExpenseUSD' })
    .stores('AssetUSDAccountID', 'data.id')
    .returns('data.id');

  const transactionId = await pactum.spec('Create Transaction', { '@DATA:TEMPLATE@': 'Transaction:MultiCurrency' })
    .returns('data.id');

  await pactum.spec('update')
    .put('/account/{id}')
    .withPathParams('id', accountId)
    .withJson({
      '@DATA:TEMPLATE@': 'Account:Asset',
      '@OVERRIDES@': {
        data: {
          attributes: {
            currency_id: 840
          }
        }
      }
    })
    .expectJson('data.attributes.currency_id', 840)
    .expectJson('data.attributes.balance', 203);

  await pactum.spec('read')
    .get('/transaction/{id}')
    .withPathParams('id', transactionId)
    .expectJsonLike('data.attributes.operations[*].rate', [1, 1]);
}).timeout(15000);

it.skip('Transaction with multiple currencies have rate recalculated', async () => {
  /* This is broken on the backend side and completely untestable, just copying the reasteasy test there

            given: "We have EUR and USD account with transaction on them"
        def eurId = AccountFixture.create(eurAccount())
        def czkId = AccountFixture.create(czkAccount())
        def usdId = AccountFixture.create(usdAccount())

        def tx = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : '2017-02-06T16:45:36',
                                "comment"   : "Multi currency",
                                "tags"      : ["spend"],
                                "operations": [
                                        [
                                                "account_id": eurId,
                                                "amount"    : 100,
                                                "rate"      : 25
                                        ],
                                        [
                                                "account_id": usdId,
                                                "amount"    : 100,
                                                "rate"      : 20
                                        ],
                                        [
                                                "account_id": czkId,
                                                "amount"    : -4500,
                                                "rate"      : 1
                                        ]
                                ]
                        ]
                ]
        ]
        def txId = TransactionFixture.create(tx)

        when: "CZK account is changed to USD account"
        def czk2usd = czkAccount()
        czk2usd.data.attributes.currency_id = 840
        given().body(JsonOutput.toJson(czk2usd))
                .when().put(API.Account, czkId)
                .then().spec(modifySpec())
                .body("data.attributes.currency_id", equalTo(840))

        then: "Balance is recalculated to new currency"
        when().get(API.Account, czkId).
                then().spec(readSpec())
                .body("data.attributes.balance", equalTo(-219))

        when().get(API.Account, eurId).
                then().spec(readSpec())
                .body("data.attributes.balance", equalTo(100))

        when().get(API.Account, usdId).
                then().spec(readSpec())
                .body("data.attributes.balance", equalTo(100))

        when().get(API.Transaction, txId)
                .then().spec(readSpec())
                .body("data.attributes.operations.find {it.account_id==${eurId}}.rate.toString()", equalTo("1.19"))
                .body("data.attributes.operations.find {it.account_id==${usdId}}.rate", equalTo(1))
                .body("data.attributes.operations.find {it.account_id==${czkId}}.rate", equalTo(1))
     */
}).timeout(15000);
