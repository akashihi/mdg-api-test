package org.akashihi.mdg.apitest.tests.budget

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.BudgetFixture
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import org.akashihi.mdg.apitest.util.RateConversion
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.closeTo
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class BudgetCalculationsSpecification extends Specification {

    static BudgetFixture bFixture = new BudgetFixture();
    static TransactionFixture tFixture = new TransactionFixture();
    static def accounts
    static def rateConverter


    def setupSpec() {
        setupAPI()

        rateConverter = new RateConversion();

        accounts = tFixture.prepareAccounts()
        bFixture.makeBudget(bFixture.aprBudget)
    }

    def cleanupSpec() {
        bFixture.removeBudget("20170401")
    }


    def 'Budget incoming amount should be sum of all asset accounts before budget term'() {
        given: 'Sum all transactions before budget'
        def acountResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/account")
        def accountBody = JsonPath.parse(acountResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def assetAccountIds = accountBody.read("data[?(@.attributes.account_type == 'asset')].id", List.class)

        def txResponse = given()
                .queryParam("notLater", '2017-04-01T00:00:00')
                .contentType("application/vnd.mdg+json").
                when()
                .get("/transaction")

        def txBody = JsonPath.parse(txResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def ops = txBody.read("data[*].attributes.operations", List.class)
        def incomingAmount = BigDecimal.ZERO
        ops.each{ x ->
            x.each { op ->
                if (assetAccountIds.contains(op['account_id'])) {
                    def opAmount = new BigDecimal(op['amount'])
                    opAmount = rateConverter.applyRate(opAmount, rateConverter.getCurrencyForAccount(op['account_id']))
                    incomingAmount = incomingAmount.add(opAmount)
                }
            }
        }

        when: 'A new budget is created and retrieved'
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20170401")


        then: "Budget incoming amount should match sum of asset transactions"
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("budget"))
                .extract().asString())
        def actualAmount = body.read("data.attributes.incoming_amount", BigDecimal.class)

        assertThat(actualAmount.compareTo(incomingAmount), is(0))
    }

    def 'Budget expected amount should be sum of all asset accounts after budget term'() {
        given: 'Some budget'
        def budgetResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20170401")
        def budgetBody = JsonPath.parse(budgetResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def incomingAmount = budgetBody.read("data.attributes.incoming_amount")
        def expectedAmount = budgetBody.read("data.attributes.outgoing_amount.expected")

        when: "List of budget entries is summarized"
        def listResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/20170401/entry")

        def listBody =  JsonPath.parse(listResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())

        def amounts = listBody.read("data[*].attributes.expected_amount", List.class)
        def calculatedAmount = BigDecimal.ZERO
        amounts.each{ x ->
            calculatedAmount = calculatedAmount.add(new BigDecimal(calculatedAmount))
        }

        then: "Budget expected amount and calculated amount should match"
        assertThat(new BigDecimal(incomingAmount).add(calculatedAmount), equalTo(new BigDecimal(expectedAmount)))
    }

    def 'Budget expected should change when budget entry is modified'() {
        given: 'Some budget'
        def budgetResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20170401")
        def budgetBody = JsonPath.parse(budgetResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def expectedAmount = budgetBody.read("data.attributes.outgoing_amount.expected")

        when: "Budget entry is modified"
        def listResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/20170401/entry")
        def listBody =  JsonPath.parse(listResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def entryId = listBody.read("data.*.id", List.class).first()
        def entryResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/20170401/entry/{id}", entryId)

        def entryBody = JsonPath.parse(entryResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def entryAmount = entryBody.read("data.attributes.expected_amount")
        def entryAccount = entryBody.read("data.attributes.account_id")
        def newAmount = new BigDecimal(entryAmount).add(9000)


        def newEntry = [
                "data": [
                        "type"      : "budgetentry",
                        "attributes": [
                                "even_distribution" : false,
                                "expected_amount" : newAmount.toString(),
                        ]
                ]
        ]
        given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(newEntry))
                .put("/budget/20170301/entry/{id}", entryId).
                then()
                .assertThat().statusCode(202)

        then: "Budget expected amount should differ by 9000"
        def updatedResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20170401")
        def updatedBody = JsonPath.parse(updatedResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def updatedAmount = updatedBody.read("data.attributes.outgoing_amount.expected")

        def expectedDifference = rateConverter.applyRate(9000, rateConverter.getCurrencyForAccount(entryAccount))

        assertThat(new BigDecimal(updatedAmount).subtract(new BigDecimal(expectedAmount)).abs(), closeTo(new BigDecimal(expectedDifference), 0.001))
    }
}
