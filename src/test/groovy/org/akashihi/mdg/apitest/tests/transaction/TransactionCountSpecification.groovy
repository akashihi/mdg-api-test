package org.akashihi.mdg.apitest.tests.transaction

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.modifySpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class TransactionCountSpecification extends Specification {
    def setupSpec() {
        setupAPI()
    }

    def "User creates new transaction"() {
        given: "Some existing transactions"
        def initialCount = given().when().get(API.Transactions)
                .then().spec(readSpec())
                .extract().path("count")

        when: "New transaction is submitted"
        TransactionFixture.create()

        then: "Transaction count is increased"
        def count = given().when().get(API.Transactions)
                .then().spec(readSpec())
                .extract().path("count")
        assertThat(count, equalTo(initialCount+1))
    }

    def "User modifies transaction data"() {
        given: "New transaction is submitted"
        def txId = TransactionFixture.create()
        def initialCount = given().when().get(API.Transactions)
                .then().spec(readSpec())
                .extract().path("count")

        when: "Transaction is modified"
        def transaction = TransactionFixture.rentTransaction()
        transaction.data.attributes.comment = "Modified"
        given().body(JsonOutput.toJson(transaction))
                .when().put(API.Transaction, txId)
                .then().spec(modifySpec())

        then: "Transaction count should not change"
        def count = given().when().get(API.Transactions)
                .then().spec(readSpec())
                .extract().path("count")
        assertThat(count, equalTo(initialCount))
    }

    def "User deletes a transaction"() {
        given: "New transaction is submitted"
        def txId = TransactionFixture.create()
        def initialCount = given().when().get(API.Transactions)
                .then().spec(readSpec())
                .extract().path("count")


        when: "Transaction is deleted"
        when().delete(API.Transaction, txId)
                .then().statusCode(204)

        then: "Transaction count should decrease"
        def count = given().when().get(API.Transactions)
                .then().spec(readSpec())
                .extract().path("count")
        assertThat(count, equalTo(initialCount-1))
    }

}
