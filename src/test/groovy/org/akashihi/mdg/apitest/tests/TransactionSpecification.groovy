package org.akashihi.mdg.apitest.tests

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.*

import java.time.LocalDateTime

import static io.restassured.RestAssured.*
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class TransactionSpecification extends Specification {

    TransactionFixture f = new TransactionFixture();

    def setupSpec() {
        setupAPI();
    }

    def "User creates new transaction"() {
        given: "Several accounts"
        def accounts = f.prepareAccounts()

        when: "New transaction on those accounts is submitted"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : '2017-02-05T16:45:36',
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
        def response =given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post("/transaction")
        def body = JsonPath.parse(response.
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .assertThat().header("Location", containsString("/api/transaction/"))
                .extract().asString())

        assertThat(body.read("data.type"), equalTo("transaction"))
        assertThat(body.read("data.attributes.comment"), equalTo("Test transaction"))
        assertThat(body.read("data.attributes.tags"), containsInAnyOrder("test", "transaction"))
        assertThat(body.read("data.attributes.operations.*.amount"), containsInAnyOrder(-150, 50, 100))
        def txId =response.then().extract().path("data.id")

        then: "Transaction appears on transaction list"
        def listResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/transaction")
        def listBody =  JsonPath.parse(listResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        assertThat(listBody.read("data", List.class).size(), is(not(0)))
        assertThat(listBody.read("data[?(@.id == ${txId})].type", List.class).first(), equalTo("transaction"))
        assertThat(listBody.read("data[?(@.id == ${txId})].attributes.comment", List.class).first(), equalTo("Test transaction"))
        assertThat(listBody.read("data[?(@.id == ${txId})].attributes.tags", List.class).first(), containsInAnyOrder("test", "transaction"))
        assertThat(listBody.read("data[?(@.id == ${txId})].attributes.operations.*.amount"), containsInAnyOrder(-150, 50, 100))
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
                                "timestamp" : '2017-02-05T16:45:36',
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
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/transaction")
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())

        assertThat(body.read("data[?(@.id == ${txId})]", List.class), empty())
    }
}

