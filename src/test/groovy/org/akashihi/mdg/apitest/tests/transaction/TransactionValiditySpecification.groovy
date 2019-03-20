package org.akashihi.mdg.apitest.tests.transaction

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.errorSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI

class TransactionValiditySpecification extends Specification {
    def setupSpec() {
        setupAPI()
    }

    def "Empty transactions are not allowed"() {
        when: "Transaction without operations is submitted"
        def transaction = TransactionFixture.rentTransaction()
        transaction.data.attributes.operations = new ArrayList()

        then: "Error should be returned"
        given().body(JsonOutput.toJson(transaction))
                .when().post(API.Transactions)
                .then().spec(errorSpec(412, "TRANSACTION_EMPTY"))
    }

    def "Empty operations are ignored"() {
        when: "Transaction without operations is submitted"
        def transaction = TransactionFixture.rentTransaction()
        transaction.data.attributes.operations[0].amount = 0
        transaction.data.attributes.operations[1].amount = 0
        transaction.data.attributes.operations[2].amount = 0

        then: "Error should be returned"
        given().body(JsonOutput.toJson(transaction))
                .when().post(API.Transactions)
                .then().spec(errorSpec(412, "TRANSACTION_EMPTY"))

    }

    def "Unbalanced transactions are not allowed"() {
        when: "Unbalanced transaction is submitted"
        def transaction = TransactionFixture.rentTransaction()
        transaction.data.attributes.operations[0].amount = -50
        transaction.data.attributes.operations[1].amount = 100
        transaction.data.attributes.operations[2].amount = 25

        then: "Error should be returned"
        given().body(JsonOutput.toJson(transaction))
                .when().post(API.Transactions)
                .then().spec(errorSpec(412, "TRANSACTION_NOT_BALANCED"))
    }
}
