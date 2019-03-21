package org.akashihi.mdg.apitest.tests.budget

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.BudgetFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.createSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*

class BudgetSpecification extends Specification {
    def setupSpec() {
        setupAPI()
    }

    def cleanupSpec() {
        BudgetFixture.remove("20170204")
        BudgetFixture.remove("20170207")
    }

    def "User creates new budget"() {
        when: "New budget is submitted to the system"
        def budget = BudgetFixture.budget("2017-02-04", "2017-02-06")
        def bId =given().body(JsonOutput.toJson(budget))
                .when().post(API.Budgets)
                .then().spec(createSpec("/api/budget"))
                .body("data.type", equalTo("budget"))
                .body("data.attributes.term_beginning", equalTo("2017-02-04"))
                .body("data.attributes.term_end", equalTo("2017-02-06"))
                .extract().path("data.id")

        then: "Budget appears on budget list"
        when().get(API.Budgets)
                .then().spec(readSpec())
                .body("data.find {it.id==${bId}}.type", equalTo("budget"))
                .body("data.find {it.id==${bId}}.attributes.term_beginning", equalTo("2017-02-04"))
                .body("data.find {it.id==${bId}}.attributes.term_end", equalTo("2017-02-06"))
    }

    def "User checks budget data"() {
        given: "Brand new budget"
        BudgetFixture.create(BudgetFixture.budget('2017-02-07', '2017-02-09'))

        when: "Budget is requested by specifying it's data"
        def response = when().get(API.Budget, "20170208")


        then: "Budget data should be returned"
        response.then().spec(readSpec())
                .body("data.type", equalTo("budget"))
                .body("data.attributes.term_beginning", equalTo("2017-02-07"))
                .body("data.attributes.term_end", equalTo("2017-02-09"))
    }

    def "User deletes budget"() {
        given: "Brand new febBudget"
        BudgetFixture.create(BudgetFixture.budget('2017-02-10', '2017-02-12'))

        when: "Budget is deleted"
        when().delete(API.Budget, "20170210")
                .then().statusCode(204)


        then: "it should disappear from budget list"
        when().get(API.Budgets)
                .then().spec(readSpec())
                .body("data.find {it.id==20170210}", nullValue())
    }
}
