package org.akashihi.mdg.apitest.tests.budget

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.AccountFixture
import org.akashihi.mdg.apitest.fixtures.BudgetFixture
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import org.akashihi.mdg.apitest.util.RateConversion
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.modifySpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat

class BudgetTransactionSpecification extends Specification {
    static def incomeId
    static def assetId
    static def expenseId
    static def transaction
    static def rateConverter

    def setupSpec() {
        setupAPI()
        rateConverter = new RateConversion()
        incomeId = AccountFixture.create(AccountFixture.incomeAccount())
        assetId = AccountFixture.create(AccountFixture.assetAccount())
        expenseId = AccountFixture.create(AccountFixture.expenseAccount())
        BudgetFixture.create(BudgetFixture.budget('2017-04-01', '2017-04-30'))

        transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2017-04-02T16:45:36",
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": incomeId,
                                                "amount"    : -150
                                        ],
                                        [
                                                "account_id": assetId,
                                                "amount"    : 50
                                        ],
                                        [
                                                "account_id": expenseId,
                                                "amount"    : 100
                                        ]
                                ]
                        ]
                ]
        ]

    }

    def cleanupSpec() {
        BudgetFixture.remove("20170401")
    }

    def 'Budget actual amount should change when transaction is submitted'() {
        given: 'An new budget'
        BigDecimal initialAmount = when().get(API.Budget, "20170401")
                .then().spec(readSpec())
                .extract().path("data.attributes.outgoing_amount.actual")

        when: "New transaction is submitted"
        def txId = TransactionFixture.create(transaction)

        then: "Actual amount should change"
        BigDecimal actualAmount = when().get(API.Budget, "20170401")
                .then().spec(readSpec())
                .extract().path("data.attributes.outgoing_amount.actual")

        BigDecimal expected = rateConverter.applyRate(50, rateConverter.getCurrencyForAccount(assetId))

        assertThat(actualAmount.subtract(initialAmount).toBigInteger(), equalTo(expected.toBigInteger()))

        when().delete(API.Transaction, txId)
    }

    def 'Budget actual amount should change when transaction is removed'() {
        given: 'An new budget with some transaction'
        def txId = TransactionFixture.create(transaction)

        BigDecimal initialAmount = when().get(API.Budget, "20170401")
                .then().spec(readSpec())
                .extract().path("data.attributes.outgoing_amount.actual")

        when: "Transaction is removed"
        when().delete(API.Transaction, txId)
        .then().statusCode(204)


        then: "Actual amount should change"
        BigDecimal actualAmount = when().get(API.Budget, "20170401")
                .then().spec(readSpec())
                .extract().path("data.attributes.outgoing_amount.actual")

        BigDecimal expected = rateConverter.applyRate(-50, rateConverter.getCurrencyForAccount(assetId))

        assertThat(actualAmount.subtract(initialAmount).toBigInteger(), equalTo(expected.toBigInteger()))
    }

    def 'Budget actual amount should change when transaction is edited'() {
        given: 'An new budget with some transaction'
        def txId = TransactionFixture.create(transaction)

        BigDecimal initialAmount = when().get(API.Budget, "20170401")
                .then().spec(readSpec())
                .extract().path("data.attributes.outgoing_amount.actual")

        when: "Transaction is edited"
        def newTx = transaction.clone()
        newTx.data.attributes.operations[1].amount = 100
        newTx.data.attributes.operations[2].amount = 50
        given().body(JsonOutput.toJson(newTx))
                .when().put(API.Transaction, txId)
                .then().spec(modifySpec())

        then: "Actual amount should change"
        BigDecimal actualAmount = when().get(API.Budget, "20170401")
                .then().spec(readSpec())
                .extract().path("data.attributes.outgoing_amount.actual")

        BigDecimal expected = rateConverter.applyRate(50, rateConverter.getCurrencyForAccount(assetId))

        assertThat(actualAmount.subtract(initialAmount).toBigInteger(), equalTo(expected.toBigInteger()))
    }
}
