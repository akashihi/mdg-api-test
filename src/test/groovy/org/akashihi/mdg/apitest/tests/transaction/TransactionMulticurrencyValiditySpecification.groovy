package org.akashihi.mdg.apitest.tests.transaction

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.equalTo

class TransactionMulticurrencyValiditySpecification extends Specification {
    TransactionFixture f = new TransactionFixture();

    def setupSpec() {
        setupAPI();
    }

    def "Multi currency transaction without rate are not allowed"() {
        given: "Several accounts"
        def accounts = f.prepareAccounts()

        when: "Transaction without rate on two is submitted"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2017-02-05T13:54:35",
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": accounts["asset"],
                                                "amount"    : -100
                                        ],
                                        [
                                                "account_id": accounts["usdAsset"],
                                                "amount"    : 120
                                        ]
                                ]

                        ]
                ]
        ]
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post(API.Transactions)
        then: "Error should be returned"
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("TRANSACTION_AMBIGUOUS_RATE"))
    }

    def "Transactions without currency with default rate are not allowed"() {
        given: "Several accounts"
        def accounts = f.prepareAccounts()

        when: "Transaction without default currency is submitted"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2017-02-05T13:54:35",
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": accounts["asset"],
                                                "rate": 2,
                                                "amount"    : -100
                                        ],
                                        [
                                                "account_id": accounts["usdAsset"],
                                                "rate": 3,
                                                "amount"    : 120
                                        ]
                                ]

                        ]
                ]
        ]
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post(API.Transactions)
        then: "Error should be returned"
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("TRANSACTION_NO_DEFAULT_RATE"))
    }

    def "Transactions with default rate on different curencies are not allowed"() {
        given: "Several accounts"
        def accounts = f.prepareAccounts()

        when: "Transaction with two default currencies is submitted"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2017-02-05T13:54:35",
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": accounts["asset"],
                                                "rate": 1,
                                                "amount"    : -100
                                        ],
                                        [
                                                "account_id": accounts["usdAsset"],
                                                "amount"    : 120
                                        ]
                                ]

                        ]
                ]
        ]
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post(API.Transactions)
        then: "Error should be returned"
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("TRANSACTION_AMBIGUOUS_RATE"))
    }

    def "Transactions with zero rate are not allowed"() {
        given: "Several accounts"
        def accounts = f.prepareAccounts()

        when: "Transaction with zero rate is submitted"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2017-02-05T13:54:35",
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": accounts["asset"],
                                                "rate": 0,
                                                "amount"    : -100
                                        ],
                                        [
                                                "account_id": accounts["usdAsset"],
                                                "amount"    : 120
                                        ]
                                ]

                        ]
                ]
        ]
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post(API.Transactions)
        then: "Error should be returned"
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("TRANSACTION_ZERO_RATE"))
    }

    def "Unbalanced transactions are not allowed"() {
        given: "Several accounts"
        def accounts = f.prepareAccounts()

        when: "Unbalanced transaction is submitted"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2017-02-05T13:54:35",
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": accounts["asset"],
                                                "amount"    : -100
                                        ],
                                        [
                                                "account_id": accounts["usdAsset"],
                                                "rate": 2,
                                                "amount"    : 120
                                        ]
                                ]

                        ]
                ]
        ]
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post(API.Transactions)
        then: "Error should be returned"
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("TRANSACTION_NOT_BALANCED"))
    }
}
