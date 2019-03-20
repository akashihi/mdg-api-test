package org.akashihi.mdg.apitest.tests.transaction

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import org.akashihi.mdg.apitest.API
import spock.lang.*

import static io.restassured.RestAssured.*
import static org.akashihi.mdg.apitest.apiConnectionBase.createSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.modifySpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*

class TransactionSpecification extends Specification {
    def setupSpec() {
        setupAPI()
    }

    def "User creates new transaction"() {
        when: "New transaction is submitted"
        def transaction = TransactionFixture.rentTransaction()
        def txId = given().body(JsonOutput.toJson(transaction))
                .when().post(API.Transactions)
                .then().spec(createSpec("/api/transaction/"))
                .body("data.type", equalTo("transaction"))
                .body("data.attributes.comment", equalTo("Test transaction"))
                .body("data.attributes.tags", containsInAnyOrder("test", "transaction"))
                .body("data.attributes.operations.findAll().amount", containsInAnyOrder(-150, 50, 100))
                .extract().path("data.id")

        then: "Transaction appears on transaction list"
        given().when().get(API.Transactions)
                .then().spec(readSpec())
                .body("data.find {it.id==${txId}}.type", equalTo("transaction"))
                .body("data.find {it.id==${txId}}.attributes.comment", equalTo("Test transaction"))
                .body("data.find {it.id==${txId}}.attributes.tags", containsInAnyOrder("test", "transaction"))
                .body("data.find {it.id==${txId}}.attributes.operations.findAll().amount", containsInAnyOrder(-150, 50, 100))
    }

    def "User checks transaction data"() {
        when: "New transaction is submitted"
        def txId = TransactionFixture.create(TransactionFixture.rentTransaction())

        then: "Transaction object should be returned"
        given().when().get(API.Transaction, txId)
            .then().spec(readSpec())
                .body("data.type", equalTo("transaction"))
                .body("data.attributes.comment", equalTo("Test transaction"))
                .body("data.attributes.tags", containsInAnyOrder("test", "transaction"))
                .body("data.attributes.operations[0].amount", is(-150))
                .body("data.attributes.operations[1].amount", is(50))
                .body("data.attributes.operations[2].amount", is(100))
    }

    def "User modifies transaction data"() {
        given: "New transaction is submitted"
        def txId = TransactionFixture.create()

        when: "Transaction is modified"
        def modified = TransactionFixture.rentTransaction()
        modified.data.attributes.comment = "Modified"
        modified.data.attributes.operations[1].amount = 80
        modified.data.attributes.operations[2].amount = 70
        given().body(JsonOutput.toJson(modified))
                .when().put(API.Transaction, txId)
                .then().spec(modifySpec())
                .body("data.attributes.comment", equalTo("Modified"))
                .body("data.attributes.operations[1].amount", is(80))
                .body("data.attributes.operations[2].amount", is(70))

        then: "Transaction object should contain new data"
        given().when().get(API.Transaction, txId)
                .then().spec(readSpec())
                .body("data.attributes.comment", equalTo("Modified"))
                .body("data.attributes.operations[1].amount", is(80))
                .body("data.attributes.operations[2].amount", is(70))
    }

    def "User deletes a transaction"() {
        given: "New transaction is submitted"
        def txId = TransactionFixture.create()

        when: "Transaction is deleted"
        when().delete(API.Transaction, txId)
            .then().statusCode(204)

        then: "Transaction should not appear in transaction list"
        given().when().get(API.Transactions)
            .then().spec(readSpec())
            .body("data.find {it.id==${txId}}", nullValue())
    }
}

