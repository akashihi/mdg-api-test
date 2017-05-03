package org.akashihi.mdg.apitest.tests.budget

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.BudgetFixture
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat

class BudgetTransactionSpecification extends Specification {
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

    def 'Budget actual amount should change when transaction is submitted'() {
        given: 'An new budget'
        def budgetResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20170401")
        def budgetBody = JsonPath.parse(budgetResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def initialAmount = budgetBody.read("data.attributes.outgoing_amount.actual", BigDecimal.class)

        when: "New transaction is submitted"
        tFixture.makeTransaction(transaction)

        then: "Actual amount should change"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20170401")
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def actualAmount = body.read("data.attributes.outgoing_amount.actual", BigDecimal.class)

        assertThat(actualAmount.subtract(initialAmount), equalTo(new BigDecimal(50)))

    }

    def 'Budget actual amount should change when transaction is removed'() {
        given: 'An new budget with some transaction'
        def txId = tFixture.makeTransaction(transaction)

        def budgetResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20170401")
        def budgetBody = JsonPath.parse(budgetResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def initialAmount = budgetBody.read("data.attributes.outgoing_amount.actual", BigDecimal.class)

        when: "Transaction is removed"
        when().delete("/transaction/{id}", txId)
        .then().assertThat().statusCode(204)


        then: "Actual amount should change"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20170401")
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def actualAmount = body.read("data.attributes.outgoing_amount.actual", BigDecimal.class)

        assertThat(actualAmount.subtract(initialAmount), equalTo(new BigDecimal(-50)))
    }

    def 'Budget actual amount should change when transaction is edited'() {
        given: 'An new budget with some transaction'
        def txId = tFixture.makeTransaction(transaction)
        def budgetResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20170401")
        def budgetBody = JsonPath.parse(budgetResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def initialAmount = budgetBody.read("data.attributes.outgoing_amount.actual", BigDecimal.class)

        when: "Transaction is edited"
        def newTx = transaction.clone()
        newTx.data.attributes.operations[1].amount = 100
        newTx.data.attributes.operations[2].amount = 50
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(newTx))
                .put("/transaction/{id}", txId)


        then: "Actual amount should change"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20170401")
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def actualAmount = body.read("data.attributes.outgoing_amount.actual", BigDecimal.class)

        assertThat(actualAmount.subtract(initialAmount), equalTo(new BigDecimal(50)))
    }
}
