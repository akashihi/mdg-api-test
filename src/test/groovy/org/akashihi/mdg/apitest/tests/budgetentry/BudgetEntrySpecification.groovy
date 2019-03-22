package org.akashihi.mdg.apitest.tests.budgetentry

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.AccountFixture
import org.akashihi.mdg.apitest.fixtures.BudgetFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.modifySpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*

class BudgetEntrySpecification extends Specification {
    static def incomeId
    static def assetId
    static def expenseId


    def setupSpec() {
        setupAPI()

        incomeId = AccountFixture.create(AccountFixture.incomeAccount())
        assetId = AccountFixture.create(AccountFixture.assetAccount())
        expenseId = AccountFixture.create(AccountFixture.expenseAccount())


        BudgetFixture.create(BudgetFixture.budget('2017-03-01', '2017-03-31'))
    }

    def cleanupSpec() {
        BudgetFixture.remove("20170301")
    }

    def 'Budget entries are created for non-asset accounts during budget creation'() {
        when: 'BudgetEntry list is requested'
        def listBody =when().get(API.BudgetEntries, "20170301")
                .then().spec(readSpec())

        then: 'BudgetEntries for non-asset accounts should be in the list'
        listBody.body("data.size()", is(not(0)))
        listBody.body("data.findAll().type.first()", equalTo("budgetentry"))

        listBody.body("data.find {it.attributes.account_id == ${incomeId}}", not(empty()))
        listBody.body("data.find {it.attributes.account_id == ${expenseId}}", not(empty()))

        listBody.body("data.find {it.attributes.account_id == ${incomeId}}.attributes.account_name", equalTo("Salary"))
        listBody.body("data.find {it.attributes.account_id == ${expenseId}}.attributes.account_name", equalTo("Rent"))

        listBody.body("data.find {it.attributes.account_id == ${incomeId}}.attributes.account_type", equalTo("income"))
        listBody.body("data.find {it.attributes.account_id == ${expenseId}}.attributes.account_type", equalTo("expense"))
    }

    def 'Budget entries are created in existing budget when new account is created'() {
        given: "Existing budget and new accounts"
        def newIncomeId = AccountFixture.create(AccountFixture.incomeAccount())
        def newExpenseId = AccountFixture.create(AccountFixture.expenseAccount())

        when: 'BudgetEntry list is requested'
        def listBody =when().get(API.BudgetEntries, "20170301")
                .then().spec(readSpec())

        then: 'BudgetEntries for non-asset accounts should be in the list'

        listBody.body("data.find {it.attributes.account_id == ${newIncomeId}}", not(empty()))
        listBody.body("data.find {it.attributes.account_id == ${newExpenseId}}", not(empty()))

        listBody.body("data.find {it.attributes.account_id == ${newIncomeId}}.attributes.account_name", equalTo("Salary"))
        listBody.body("data.find {it.attributes.account_id == ${newExpenseId}}.attributes.account_name", equalTo("Rent"))

        listBody.body("data.find {it.attributes.account_id == ${newIncomeId}}.attributes.account_type", equalTo("income"))
        listBody.body("data.find {it.attributes.account_id == ${newExpenseId}}.attributes.account_type", equalTo("expense"))
    }

    def 'Budget entries should be accessible by id'() {
        given: "List of budget entries"
        def entryId = when().get(API.BudgetEntries, "20170301")
                .then().spec(readSpec())
                .extract().path("data.findAll().id").first()

        when: "Entry is requested"
        def response = when().get(API.BudgetEntry, "20170301", entryId)

        then: "Entry should be returned"
        response.then().spec(readSpec())
                .body("data.id", equalTo(entryId))
                .body("data.type", equalTo("budgetentry"))
    }

    def 'Budget entries should be changeable by id'() {
        given: "Some budget entry"
        def entryId = when().get(API.BudgetEntries, "20170301")
                .then().spec(readSpec())
                .extract().path("data.findAll().id").first()

        when: "Entry is updated"
        def newEntry = [
                "data": [
                        "type"      : "budgetentry",
                        "attributes": [
                                "expected_amount" : 9000,
                        ]
                ]
        ]
        given().body(JsonOutput.toJson(newEntry))
                .when().put(API.BudgetEntry, "20170301", entryId)
                .then().spec(modifySpec())
                .body("data.attributes.expected_amount", is(9000))

        then: "New values should be returned"
        when().get(API.BudgetEntry, "20170301", entryId)
                .then().spec(readSpec())
                .body("data.attributes.expected_amount", equalTo(9000))
    }

    def 'Removing even flasg should also remove proration'() {
        given: "Some budget entry"
        def entryId = when().get(API.BudgetEntries, "20170301")
                .then().spec(readSpec())
                .extract().path("data.findAll().id").first()

        when: "Entry is updated"
        def newEntry = [
                "data": [
                        "type"      : "budgetentry",
                        "attributes": [
                                "even_distribution" : false,
                                "proration": true
                        ]
                ]
        ]
        given().body(JsonOutput.toJson(newEntry))
                .when().put(API.BudgetEntry, "20170301", entryId).
                then().spec(modifySpec())
                .body("data.attributes.even_distribution", is(false))
                .body("data.attributes.proration", is(false))

        then: "New values should be returned"
        when().get(API.BudgetEntry, "20170301", entryId)
                .then().spec(readSpec())
                .body("data.attributes.even_distribution", is(false))
                .body("data.attributes.proration", is(false))
    }
}
