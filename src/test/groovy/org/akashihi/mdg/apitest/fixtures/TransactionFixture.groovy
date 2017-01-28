package org.akashihi.mdg.apitest.fixtures

import groovy.json.JsonOutput

import java.time.LocalDateTime

import static io.restassured.RestAssured.given

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

    private static def makeAccount(account) {
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(account))
                .post("/account").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")
    }

    public def prepareAccounts() {
        ["income": makeAccount(accountIncome), "asset": makeAccount(accountAsset), "expense": makeAccount(accountExpense)]
    }

    private static def makeTransaction(transaction) {
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post("/transaction").
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().path("data.id")
    }

    public def makeRentTransaction() {
        def accounts = prepareAccounts()
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : LocalDateTime.now(),
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": accounts["income"],
                                                "amount"    : -150
                                        ],
                                        [
                                                "account_id": accounts["asset"],
                                                "amount"    : 50
                                        ],
                                        [
                                                "account_id": accounts["expense"],
                                                "amount"    : 100
                                        ]
                                ]
                        ]
                ]
        ]
        makeTransaction(transaction)
    }

    public def makeIncomeTransaction() {
        def accounts = prepareAccounts()
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : LocalDateTime.now().plusDays(1),
                                "comment"   : "Income transaction",
                                "tags"      : ["income", "transaction"],
                                "operations": [
                                        [
                                                "account_id": accounts["income"],
                                                "amount"    : -150
                                        ],
                                        [
                                                "account_id": accounts["asset"],
                                                "amount"    : 150
                                        ]
                                ]
                        ]
                ]
        ]
        makeTransaction(transaction)
    }

    public def makeSpendTransaction() {
        def accounts = prepareAccounts()
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : LocalDateTime.now().plusDays(1),
                                "comment"   : "Spend transaction",
                                "tags"      : ["spend"],
                                "operations": [
                                        [
                                                "account_id": accounts["asset"],
                                                "amount"    : -150
                                        ],
                                        [
                                                "account_id": accounts["expense"],
                                                "amount"    : 150
                                        ]
                                ]
                        ]
                ]
        ]
        makeTransaction(transaction)
    }

    public def makeTransactions() {
        return [makeRentTransaction(), makeIncomeTransaction(), makeSpendTransaction()]
    }
}
