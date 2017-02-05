package org.akashihi.mdg.apitest.tests

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.equalTo

class TransactionValiditySpecification extends Specification {

    TransactionFixture f = new TransactionFixture();

    def setupSpec() {
        setupAPI();
    }

    def "Empty transactions are not allowed"() {
        given: "Several accounts"
        f.prepareAccounts()

        when: "Transaction without operations is submitted"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2017-02-05T13:54:35",
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": new ArrayList()
                        ]
                ]
        ]
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post("/transaction")
        then: "Error should be returned"
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("TRANSACTION_EMPTY"))
    }

    def "Empty operations are ignored"() {
        given: "Several accounts"
        def accounts = f.prepareAccounts()

        when: "Transaction without operations is submitted"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2017-02-05T13:54:35",
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": accounts["income"],
                                                "amount"    : 0
                                        ],
                                        [
                                                "account_id": accounts["asset"],
                                                "amount"    : 0
                                        ]
                                ]
                        ]
                ]
        ]
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post("/transaction")
        then: "Error should be returned"
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("TRANSACTION_EMPTY"))
    }

    def "Unbalanced transactions are not allowed"() {
        given: "Several accounts"
        def accounts = f.prepareAccounts()

        when: "Transaction without operations is submitted"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2017-02-05T13:54:35",
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": accounts["income"],
                                                "amount"    : -50
                                        ],
                                        [
                                                "account_id": accounts["asset"],
                                                "amount"    : 100
                                        ]
                                ]
                        ]
                ]
        ]
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post("/transaction")
        then: "Error should be returned"
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("TRANSACTION_NOT_BALANCED"))
    }
}
