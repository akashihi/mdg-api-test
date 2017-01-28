package org.akashihi.mdg.apitest.tests

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.*

import java.time.LocalDateTime

import static io.restassured.RestAssured.*
import static org.hamcrest.Matchers.*

class TransactionSpecification extends Specification {

    TransactionFixture f = new TransactionFixture();

    def "User creates new transaction"() {
        given: "Several accounts"
        def accounts = f.prepareAccounts()

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
        def txId = f.makeRentTransaction()

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
        def accounts = f.prepareAccounts()
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
        def txId = f.makeRentTransaction()

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

