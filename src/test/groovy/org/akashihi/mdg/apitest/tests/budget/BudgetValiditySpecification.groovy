package org.akashihi.mdg.apitest.tests.budget

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.BudgetFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API
import spock.lang.Unroll

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.errorSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI

class BudgetValiditySpecification extends Specification {
    def setupSpec() {
        setupAPI()
        BudgetFixture.create(BudgetFixture.budget('2017-02-13', '2017-02-28'))
    }

    def cleanupSpec() {
        BudgetFixture.remove('20170213')
    }

    @Unroll
    def "Budget validity period is #NAME"() {
        expect:
        def budget = BudgetFixture.budget(beginning, end)
        given().body(JsonOutput.toJson(budget))
                .when().post(API.Budgets)
                .then().spec(errorSpec(412, msg))

        where:
        NAME                    | beginning    | end          | msg
        "less then two days"    | '2017-02-09' | '2017-02-09' | "BUDGET_SHORT_RANGE"
        "not inverted"          | '2017-02-10' | '2017-02-09' | "BUDGET_INVALID_TERM"
        "not fully overlapping" | '2017-02-14' | '2017-02-17' | "BUDGET_OVERLAPPING"
        "not start overlapping" | '2017-02-14' | '2017-03-14' | "BUDGET_OVERLAPPING"
        "not end overlapping"   | '2017-01-14' | '2017-02-14' | "BUDGET_OVERLAPPING"
    }
}
