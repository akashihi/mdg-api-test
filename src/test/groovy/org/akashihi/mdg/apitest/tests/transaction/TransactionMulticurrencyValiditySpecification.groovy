package org.akashihi.mdg.apitest.tests.transaction

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.errorSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI

class TransactionMulticurrencyValiditySpecification extends Specification {
    def setupSpec() {
        setupAPI()
    }

    def "Multi currency transaction without rate are not allowed"() {
        when: "Transaction without rate on two is submitted"
        def transaction = TransactionFixture.multiCurrencyTransaction()
        transaction.data.attributes.operations[0].remove("rate")

        then: "Error should be returned"
        given().body(JsonOutput.toJson(transaction))
                .when().post(API.Transactions)
                .then().spec(errorSpec(412, "TRANSACTION_AMBIGUOUS_RATE"))
    }

    def "Transactions without currency with default rate are not allowed"() {
        when: "Transaction without default currency is submitted"
        def transaction = TransactionFixture.multiCurrencyTransaction()
        transaction.data.attributes.operations[1].rate = 3

        then: "Error should be returned"
        given().body(JsonOutput.toJson(transaction))
                .when().post(API.Transactions)
                .then().spec(errorSpec(412, "TRANSACTION_NO_DEFAULT_RATE"))
    }

    def "Transactions with default rate on different currencies are not allowed"() {
        when: "Transaction with two default currencies is submitted"
        def transaction = TransactionFixture.multiCurrencyTransaction()
        transaction.data.attributes.operations[0].rate = 1

        then: "Error should be returned"
        given().body(JsonOutput.toJson(transaction))
                .when().post(API.Transactions)
                .then().spec(errorSpec(412, "TRANSACTION_AMBIGUOUS_RATE"))
    }

    def "Transactions with zero rate are not allowed"() {
        when: "Transaction with zero rate is submitted"
        def transaction = TransactionFixture.multiCurrencyTransaction()
        transaction.data.attributes.operations[0].rate = 0

        then: "Error should be returned"
        given().body(JsonOutput.toJson(transaction))
                .when().post(API.Transactions)
                .then().spec(errorSpec(412, "TRANSACTION_ZERO_RATE"))
    }

    def "Unbalanced transactions are not allowed"() {
        when: "Unbalanced transaction is submitted"
        def transaction = TransactionFixture.multiCurrencyTransaction()
        transaction.data.attributes.operations[1].amount = 120

        then: "Error should be returned"
        given().body(JsonOutput.toJson(transaction))
                .when().post(API.Transactions)
                .then().spec(errorSpec(412, "TRANSACTION_NOT_BALANCED"))
    }
}
