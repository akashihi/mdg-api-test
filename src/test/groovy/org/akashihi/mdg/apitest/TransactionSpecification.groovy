package org.akashihi.mdg.apitest

import groovy.json.JsonOutput
import spock.lang.*

import java.time.LocalDateTime

import static io.restassured.RestAssured.*
import static io.restassured.matcher.RestAssuredMatchers.*
import static org.hamcrest.Matchers.*

class TransactionSpecification extends Specification {
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

    private def prepareAccounts() {
        [makeAccount(accountIncome), makeAccount(accountAsset), makeAccount(accountExpense)]
    }

    def "User creates new transaction"() {
        given: "Several accounts"
        def accounts = prepareAccounts()

        when: "New transaction on those accounts is submitted"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : LocalDateTime.now(),
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": accounts[0],
                                                "amount"    : -150
                                        ],
                                        [
                                                "account_id": accounts[1],
                                                "amount"    : 50
                                        ],
                                        [
                                                "account_id": accounts[2],
                                                "amount"    : 100
                                        ]
                                ]
                        ]
                ]
        ]
        def txId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post("/transaction").
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .assertThat().header("Location", contains("/transaction/"))
                .body("data.type", equalTo("transaction"))
                .body("data.attributes.comment", equalTo("Test transaction"))
                .body("data.attributes.tags", containsInAnyOrder("test", "transaction"))
                .body("data.attributes.operations.*.account_id", equalTo(accounts))
                .body("data.attributes.operations[0].amount", is(-150))
                .body("data.attributes.operations[1].amount", is(50))
                .body("data.attributes.operations[2].amount", is(100))
                .extract().path("data.id")

        then: "Transaction appears on transaction list"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .get("/transaction").
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.length()", not(0))
                .body("data[?(@.id == ${txId})].type", equalTo("transaction"))
                .body("data[?(@.id == ${txId})].attributes.comment", equalTo("Test transaction"))
                .body("data[?(@.id == ${txId})].attributes.tags", containsInAnyOrder("test", "transaction"))
                .body("data[?(@.id == ${txId})].attributes.operations.*.account_id", equalTo(accounts))
                .body("data[?(@.id == ${txId})].attributes.operations[0].amount", is(-150))
                .body("data[?(@.id == ${txId})].attributes.operations[1].amount", is(50))
                .body("data[?(@.id == ${txId})].attributes.operations[2].amount", is(100))
    }

    def "User checks transaction data"() {
        given: "New transaction is submitted"
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
                                                "account_id": accounts[0],
                                                "amount"    : -150
                                        ],
                                        [
                                                "account_id": accounts[1],
                                                "amount"    : 50
                                        ],
                                        [
                                                "account_id": accounts[2],
                                                "amount"    : 100
                                        ]
                                ]
                        ]
                ]
        ]
        def txId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post("/transaction").
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().path("data.id")

        when: "Specific transaction is requested"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/transaction/{id}", txId)

        then: "Transaction object should be returned"
        response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("transaction"))
                .body("data.attributes.comment", equalTo("Test transaction"))
                .body("data.attributes.tags", containsInAnyOrder("test", "transaction"))
                .body("data.attributes.operations.*.account_id", equalTo(accounts))
                .body("data.attributes.operations[0].amount", is(-150))
                .body("data.attributes.operations[1].amount", is(50))
                .body("data.attributes.operations[2].amount", is(100))
    }

    def "User modifies transaction data"() {
        given: "New transaction is submitted"
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
                                                "account_id": accounts[0],
                                                "amount"    : -150
                                        ],
                                        [
                                                "account_id": accounts[1],
                                                "amount"    : 50
                                        ],
                                        [
                                                "account_id": accounts[2],
                                                "amount"    : 100
                                        ]
                                ]
                        ]
                ]
        ]
        def txId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post("/transaction").
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().path("data.id")

        when: "Transaction is modified"
        transaction.data.attributes.comment = "Modified"
        transaction.data.attributes.operations[1].amount = 100
        transaction.data.attributes.operations[2].amount = 50
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post("/transaction/{id}", txId)

        then: "Transaction object should contain new data"
        then: "Transaction object should be returned"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/transaction/{id}", txId).
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("transaction"))
                .body("data.attributes.comment", equalTo("Modified"))
                .body("data.attributes.operations[1].amount", is(100))
                .body("data.attributes.operations[2].amount", is(50))
    }

    def "User deletes a transaction"() {
        given: "New transaction is submitted"
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
                                                "account_id": accounts[0],
                                                "amount"    : -150
                                        ],
                                        [
                                                "account_id": accounts[1],
                                                "amount"    : 50
                                        ],
                                        [
                                                "account_id": accounts[2],
                                                "amount"    : 100
                                        ]
                                ]
                        ]
                ]
        ]
        def txId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post("/transaction").
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().path("data.id")

        when: "Transaction is deleted"
        when().delete("/transaction/{id}", txId)
        .then().assertThat().statusCode(204)

        then: "Transaction should not appear in transaction list"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/transaction").
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data[?(@.id == ${txId})].length()", is(0))
    }
}

