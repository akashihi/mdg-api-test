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
import static org.hamcrest.Matchers.equalTo

class TransactionAccountSpecification extends Specification {
    TransactionFixture f = new TransactionFixture();

    def setupSpec() {
        setupAPI()
    }

    def 'New transaction changes account balance'() {
        given: "Transaction on accounts"
        def transaction = TransactionFixture.rentTransaction()
        def incomeId = transaction.data.attributes.operations[0].account_id
        def assetId = transaction.data.attributes.operations[1].account_id
        def expenseId = transaction.data.attributes.operations[2].account_id

        when: "New transaction on those accounts is submitted"
        TransactionFixture.create(transaction)

        then: "Balance on accounts is changed"
        given().when().get(API.Account, incomeId)
                .then().spec(readSpec())
                .body("data.attributes.balance", equalTo(-150))

        given().when().get(API.Account, assetId)
                .then().spec(readSpec())
                .body("data.attributes.balance", equalTo(50))

        given().when().get(API.Account, expenseId)
                .then().spec(readSpec())
                .body("data.attributes.balance", equalTo(100))
    }

    def 'Changing transaction ops is reflected on accounts'() {
        given: "New transaction is submitted"
        def transaction = TransactionFixture.rentTransaction()
        def incomeId = transaction.data.attributes.operations[0].account_id
        def assetId = transaction.data.attributes.operations[1].account_id
        def expenseId = transaction.data.attributes.operations[2].account_id

        def txId = TransactionFixture.create(transaction)

        when: "Transaction is modified"
        transaction.data.attributes.operations[0].amount = -30
        transaction.data.attributes.operations[2].amount = -20
        given().body(JsonOutput.toJson(transaction))
                .when().put(API.Transaction, txId)
                .then().spec(modifySpec())

        then: "Accounts balances should be changed"
        given().when().get(API.Account, incomeId)
                .then().spec(readSpec())
                .body("data.attributes.balance", equalTo(-30))

        given().when().get(API.Account, assetId)
                .then().spec(readSpec())
                .body("data.attributes.balance", equalTo(50))

        given().when().get(API.Account, expenseId)
                .then().spec(readSpec())
                .body("data.attributes.balance", equalTo(-20))
    }

    def 'Deleting transaction reverts changes on accounts'() {
        given: "New transaction is submitted"
        def transaction = TransactionFixture.rentTransaction()
        def incomeId = transaction.data.attributes.operations[0].account_id
        def assetId = transaction.data.attributes.operations[1].account_id
        def expenseId = transaction.data.attributes.operations[2].account_id

        def txId = TransactionFixture.create(transaction)


        when: "Transaction is deleted"
        when().delete(API.Transaction, txId)
                .then().statusCode(204)

        then: "Accounts should have zero balance"
        given().when().get(API.Account, incomeId)
                .then().spec(readSpec())
                .body("data.attributes.balance", equalTo(0))

        given().when().get(API.Account, assetId)
                .then().spec(readSpec())
                .body("data.attributes.balance", equalTo(0))

        given().when().get(API.Account, expenseId)
                .then().spec(readSpec())
                .body("data.attributes.balance", equalTo(0))
    }

}
