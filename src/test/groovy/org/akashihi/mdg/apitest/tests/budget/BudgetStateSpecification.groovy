package org.akashihi.mdg.apitest.tests.budget

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.BudgetFixture
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat

class BudgetStateSpecification extends Specification {
    static BudgetFixture bFixture = new BudgetFixture();
    static TransactionFixture tFixture = new TransactionFixture();
    static def accounts;


    def setupSpec() {
        setupAPI();
        accounts = tFixture.prepareAccounts();
        bFixture.makeBudget(bFixture.incomeStateBudget);
    }

    def cleanupSpec() {
        bFixture.removeBudget("20161201")
    }

    def 'Budget expected income should follow entries expected income'() {
        def budgetResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20161201")
        def budgetBody = JsonPath.parse(budgetResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def expectedIncome = budgetBody.read("data.attributes.state.income.expected")

        assertThat(new BigDecimal(expectedIncome), equalTo(new BigDecimal(0)))

        when: "Budget entry is modified"
        def accountId = accounts["income"]
        def listResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/20161201/entry")
        def listBody =  JsonPath.parse(listResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def entry = listBody.read("data[?(@.attributes.account_id == ${accountId})]", List.class).first()
        entry.attributes.expected_amount=3620
        given()
                .contentType("application/vnd.mdg+json")
                .when()
                .log().all()
                .request().body(JsonOutput.toJson(["data": entry]))
                .put("/budget/20161201/entry/{id}", entry.id).
                then()
                .log().all()
                .assertThat().statusCode(202)

        then: "Budget expected income should be 3620"
        def updatedResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20161201")
        def updatedBody = JsonPath.parse(updatedResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def updatedAmount = updatedBody.read("data.attributes.state.income.expected")

        assertThat(new BigDecimal(updatedAmount), equalTo(new BigDecimal(3620)))
    }

    def 'Budget actual income should follow income transactions'() {
        def budgetResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20161201")
        def budgetBody = JsonPath.parse(budgetResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def expectedIncome = budgetBody.read("data.attributes.state.income.actual")

        assertThat(new BigDecimal(expectedIncome), equalTo(new BigDecimal(0)))

        when: "Transaction os posted"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2016-12-01T16:45:36",
                                "comment"   : "Income transaction",
                                "tags": [],
                                "operations": [
                                        [
                                                "account_id": accounts["income"],
                                                "amount"    : -9000
                                        ],
                                        [
                                                "account_id": accounts["asset"],
                                                "amount"    : 9000
                                        ]
                                ]
                        ]
                ]
        ]
        tFixture.makeTransaction(transaction)

        then: "Budget expected income should be 9000"
        def updatedResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20161201")
        def updatedBody = JsonPath.parse(updatedResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def updatedAmount = updatedBody.read("data.attributes.state.income.actual")

        assertThat(new BigDecimal(updatedAmount), equalTo(new BigDecimal(9000)))
    }

    def 'Budget expected expenses should follow entries expected spendings'() {
        def budgetResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20161201")
        def budgetBody = JsonPath.parse(budgetResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def expectedExpenses = budgetBody.read("data.attributes.state.expense.expected")

        assertThat(new BigDecimal(expectedExpenses), equalTo(new BigDecimal(0)))

        when: "Budget entry is modified"
        def accountId = accounts["expense"]
        def listResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/20161201/entry")
        def listBody =  JsonPath.parse(listResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def entry = listBody.read("data[?(@.attributes.account_id == ${accountId})]", List.class).first()
        entry.attributes.expected_amount=1924
        given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(["data": entry]))
                .put("/budget/20161201/entry/{id}", entry.id).
                then()
                .assertThat().statusCode(202)

        then: "Budget expected expense should be 1924"
        def updatedResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20161201")
        def updatedBody = JsonPath.parse(updatedResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def updatedAmount = updatedBody.read("data.attributes.state.expense.expected")

        assertThat(new BigDecimal(updatedAmount), equalTo(new BigDecimal(1924)))
    }

    def 'Budget actual spendings should follow income transactions'() {
        def budgetResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20161201")
        def budgetBody = JsonPath.parse(budgetResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def expectedSpendings = budgetBody.read("data.attributes.state.expense.actual")

        assertThat(new BigDecimal(expectedSpendings), equalTo(new BigDecimal(0)))

        when: "Transaction os posted"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2016-12-01T16:45:36",
                                "comment"   : "Income transaction",
                                "tags": [],
                                "operations": [
                                        [
                                                "account_id": accounts["asset"],
                                                "amount"    : -9000
                                        ],
                                        [
                                                "account_id": accounts["expense"],
                                                "amount"    : 9000
                                        ]
                                ]
                        ]
                ]
        ]
        tFixture.makeTransaction(transaction)

        then: "Budget expected income should be 9000"
        def updatedResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20161201")
        def updatedBody = JsonPath.parse(updatedResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def updatedAmount = updatedBody.read("data.attributes.state.expense.actual")

        assertThat(new BigDecimal(updatedAmount), equalTo(new BigDecimal(9000)))
    }
}
