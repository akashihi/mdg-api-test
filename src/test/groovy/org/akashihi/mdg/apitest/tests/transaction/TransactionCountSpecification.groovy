package org.akashihi.mdg.apitest.tests.transaction

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class TransactionCountSpecification extends Specification {

    TransactionFixture f = new TransactionFixture();

    def setupSpec() {
        setupAPI();
    }

    def "User creates new transaction"() {
        given: "Some existing transactions"
        def accounts = f.prepareAccounts()
        def initialResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Transactions)
        def initialBody =  JsonPath.parse(initialResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def initialCount = initialBody.read("count")


        when: "New transaction is submitted"
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
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post(API.Transactions)

        then: "Transaction count is increased"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Transactions)
        def body =  JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def count = body.read("count")
        assertThat(count, equalTo(initialCount+1))
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
                .post(API.Transactions).
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().path("data.id")
        def initialResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Transactions)
        def initialBody =  JsonPath.parse(initialResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def initialCount = initialBody.read("count")


        when: "Transaction is modified"
        transaction.data.attributes.comment = "Modified"
        transaction.data.attributes.operations[1].amount = 100
        transaction.data.attributes.operations[2].amount = 50
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .put(API.Transaction, txId)

        then: "Transaction count should not change"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Transactions)
        def body =  JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def count = body.read("count")
        assertThat(count, equalTo(initialCount))
    }

    def "User deletes a transaction"() {
        given: "New transaction is submitted"
        def txId = f.makeRentTransaction()
        def initialResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Transactions)
        def initialBody =  JsonPath.parse(initialResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def initialCount = initialBody.read("count")


        when: "Transaction is deleted"
        when().delete(API.Transaction, txId)
                .then().assertThat().statusCode(204)

        then: "Transaction count should decrease"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Transactions)
        def body =  JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def count = body.read("count")
        assertThat(count, equalTo(initialCount-1))
    }

}
