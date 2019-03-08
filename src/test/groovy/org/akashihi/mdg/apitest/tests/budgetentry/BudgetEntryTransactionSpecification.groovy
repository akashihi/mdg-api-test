package org.akashihi.mdg.apitest.tests.budgetentry

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.BudgetFixture
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat

class BudgetEntryTransactionSpecification extends Specification {
    static BudgetFixture bFixture = new BudgetFixture();
    static TransactionFixture tFixture = new TransactionFixture();
    static def accounts;
    static def transaction;

    def setupSpec() {
        setupAPI();
        accounts = tFixture.prepareAccounts();
        bFixture.makeBudget(bFixture.aprBudget);

        transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2017-04-02T16:45:36",
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

    }

    def cleanupSpec() {
        bFixture.removeBudget("20170401")
    }

    def 'BudgetEntry actual amount should change when transaction is submitted'() {
        given: 'An new budget entry'
        def listResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/20170401/entry")
        def listBody =  JsonPath.parse(listResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def initialIncome = new BigDecimal(listBody.read("data[?(@.attributes.account_id == ${accounts['income']})].attributes.actual_amount").first())
        def initialExpense = new BigDecimal(listBody.read("data[?(@.attributes.account_id == ${accounts['expense']})].attributes.actual_amount").first())

        when: "New transaction is submitted"
        tFixture.makeTransaction(transaction)

        then: "Actual amount should change"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/20170401/entry")
        def body =  JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def actualIncome = new BigDecimal(body.read("data[?(@.attributes.account_id == ${accounts['income']})].attributes.actual_amount").first())
        def actualExpense = new BigDecimal(body.read("data[?(@.attributes.account_id == ${accounts['expense']})].attributes.actual_amount").first())


        assertThat(actualIncome.subtract(initialIncome), equalTo(new BigDecimal(150)))
        assertThat(actualExpense.subtract(initialExpense), equalTo(new BigDecimal(100)))
    }

    def 'Budget actual amount should change when transaction is removed'() {
        given: 'An new budget with some transaction'
        def txId = tFixture.makeTransaction(transaction)

        def listResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/20170401/entry")
        def listBody =  JsonPath.parse(listResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def initialIncome = new BigDecimal(listBody.read("data[?(@.attributes.account_id == ${accounts['income']})].attributes.actual_amount").first())
        def initialExpense = new BigDecimal(listBody.read("data[?(@.attributes.account_id == ${accounts['expense']})].attributes.actual_amount").first())

        when: "Transaction is removed"
        when().delete(API.Transaction, txId)
                .then().assertThat().statusCode(204)


        then: "Actual amount should change"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/20170401/entry")
        def body =  JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def actualIncome = new BigDecimal(body.read("data[?(@.attributes.account_id == ${accounts['income']})].attributes.actual_amount").first())
        def actualExpense = new BigDecimal(body.read("data[?(@.attributes.account_id == ${accounts['expense']})].attributes.actual_amount").first())

        assertThat(actualIncome.subtract(initialIncome), equalTo(new BigDecimal(-150)))
        assertThat(actualExpense.subtract(initialExpense), equalTo(new BigDecimal(-100)))
    }

    def 'Budget actual amount should change when transaction is edited'() {
        given: 'An new budget with some transaction'
        def txId = tFixture.makeTransaction(transaction)
        def listResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/20170401/entry")
        def listBody =  JsonPath.parse(listResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def initialIncome = new BigDecimal(listBody.read("data[?(@.attributes.account_id == ${accounts['income']})].attributes.actual_amount").first())
        def initialExpense = new BigDecimal(listBody.read("data[?(@.attributes.account_id == ${accounts['expense']})].attributes.actual_amount").first())

        when: "Transaction is edited"
        def newTx = transaction.clone()
        newTx.data.attributes.operations[1].amount = 100
        newTx.data.attributes.operations[2].amount = 50
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(newTx))
                .put(API.Transaction, txId)


        then: "Actual amount should change"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/20170401/entry")
        def body =  JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def actualIncome = new BigDecimal(body.read("data[?(@.attributes.account_id == ${accounts['income']})].attributes.actual_amount").first())
        def actualExpense = new BigDecimal(body.read("data[?(@.attributes.account_id == ${accounts['expense']})].attributes.actual_amount").first())

        assertThat(actualIncome, equalTo(initialIncome))
        assertThat(actualExpense.subtract(initialExpense), equalTo(new BigDecimal(-50)))
    }

}
