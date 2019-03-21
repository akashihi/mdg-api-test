package org.akashihi.mdg.apitest.fixtures

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.API

import java.time.LocalDateTime

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.createSpec

class TransactionFixture {
    def accountExpense = [
            "data": [
                    "type"      : "account",
                    "attributes": [
                            "account_type": "expense",
                            "currency_id" : 978,
                            "name"        : "Rent"
                    ]
            ]
    ]

    def accountAsset = [
            "data": [
                    "type"      : "account",
                    "attributes": [
                            "account_type": "asset",
                            "currency_id" : 978,
                            "name"        : "Current"
                    ]
            ]
    ]

    def accountIncome = [
            "data": [
                    "type"      : "account",
                    "attributes": [
                            "account_type": "income",
                            "currency_id" : 978,
                            "name"        : "Salary"
                    ]
            ]
    ]

    def accountUsdAsset = [
            "data": [
                    "type"      : "account",
                    "attributes": [
                            "account_type": "asset",
                            "currency_id" : 840,
                            "name"        : "USD current"
                    ]
            ]
    ]

    private static def makeAccount(account) {
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(account))
                .post(API.Accounts).
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")
    }

    public def prepareAccounts() {
        [
                "income": Long.valueOf(makeAccount(accountIncome)),
                "asset": Long.valueOf(makeAccount(accountAsset)),
                "usdAsset": Long.valueOf(makeAccount(accountUsdAsset)),
                "expense": Long.valueOf(makeAccount(accountExpense))
        ]
    }

    public def makeTransaction(transaction) {
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post(API.Transactions).
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().path("data.id")
    }

    static def multiCurrencyTransaction() {
        def assetId = AccountFixture.create(AccountFixture.assetAccount())
        def usdAssetId = AccountFixture.create(AccountFixture.usdAccount())
        return [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2017-02-05T13:54:35",
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": assetId,
                                                "rate": 2,
                                                "amount"    : -100
                                        ],
                                        [
                                                "account_id": usdAssetId,
                                                "amount"    : 200
                                        ]
                                ]

                        ]
                ]
        ]
    }

    static def incomeTransaction() {
        def incomeId = AccountFixture.create(AccountFixture.incomeAccount())
        def assetId = AccountFixture.create(AccountFixture.assetAccount())
        return [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : '2017-02-05T16:45:36',
                                "comment"   : "Income transaction",
                                "tags"      : ["income", "transaction"],
                                "operations": [
                                        [
                                                "account_id": incomeId,
                                                "amount"    : -150
                                        ],
                                        [
                                                "account_id": assetId,
                                                "amount"    : 150
                                        ]
                                ]
                        ]
                ]
        ]
    }

    static def rentTransaction() {
        def incomeId = AccountFixture.create(AccountFixture.incomeAccount())
        def assetId = AccountFixture.create(AccountFixture.assetAccount())
        def expenseId = AccountFixture.create(AccountFixture.expenseAccount())
        return [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2017-02-04T16:45:36",
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": incomeId,
                                                "amount"    : -150
                                        ],
                                        [
                                                "account_id": assetId,
                                                "amount"    : 50
                                        ],
                                        [
                                                "account_id": expenseId,
                                                "amount"    : 100
                                        ]
                                ]
                        ]
                ]
        ]
    }

    static def spendTransaction() {
        def assetId = AccountFixture.create(AccountFixture.assetAccount())
        def expenseId = AccountFixture.create(AccountFixture.expenseAccount())

        return [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : '2017-02-06T16:45:36',
                                "comment"   : "Spend transaction",
                                "tags"      : ["spend"],
                                "operations": [
                                        [
                                                "account_id": assetId,
                                                "amount"    : -150
                                        ],
                                        [
                                                "account_id": expenseId,
                                                "amount"    : 150
                                        ]
                                ]
                        ]
                ]
        ]
    }

    static def create() {
        create(rentTransaction())
    }

    static def createMultiple() {
        create(incomeTransaction())
        create(spendTransaction())
        create(rentTransaction())
    }

    static def create(transaction) {
        return given().body(JsonOutput.toJson(transaction))
                .when()
                .post(API.Transactions)
                .then().spec(createSpec("/api/transaction"))
                .extract().path("data.id")
    }
}
