package org.akashihi.mdg.apitest.tests

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.BudgetFixture
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.equalTo

class BudgetValiditySpecification extends Specification {
    static BudgetFixture fixture = new BudgetFixture();

    def setupSpec() {
        setupAPI();
        fixture.makeBudget(fixture.febBudget);
    }

    def "Budget validity period is less then two days"() {
        given: "Very short febBudget"
        def budget = [
                "data": [
                        "type"      : "buget",
                        "attributes": [
                                "term_beginning" : '2017-02-09',
                                "term_end" : '2017-02-09',
                        ]
                ]
        ]


        when: "Budget is submitted to the system"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(budget))
                .post("/budget")


        then: "It should not be accepted"
        response.then()
            .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("BUDGET_SHORT_RANGE"))
    }

    def "Budget should become effective before it expires"() {
        given: "Budget the expires before it's start time"
        def budget = [
                "data": [
                        "type"      : "buget",
                        "attributes": [
                                "term_beginning" : '2017-02-10',
                                "term_end" : '2017-02-09',
                        ]
                ]
        ]


        when: "Budget is submitted to the system"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(budget))
                .post("/budget")


        then: "It should not be accepted"
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("BUDGET_INVALID_TERM"))
    }

    def "Budget should not be inside other budget validity period"() {
        given: "Two overlapping budgets"
        def overlap = [
                "data": [
                        "type"      : "budget",
                        "attributes": [
                                "term_beginning" : '2017-02-14',
                                "term_end" : '2017-02-17',
                        ]
                ]
        ]


        when: "Budget is submitted to the system"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(overlap))
                .post("/budget")


        then: "It should not be accepted"
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("BUDGET_OVERLAPPING"))
    }

    def "Budget term beginning should not be inside other budget validity period"() {
        given: "Two overlapping budgets"
        def overlap = [
                "data": [
                        "type"      : "bubget",
                        "attributes": [
                                "term_beginning" : '2017-02-14',
                                "term_end" : '2017-03-14',
                        ]
                ]
        ]


        when: "Budget is submitted to the system"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(overlap))
                .post("/budget")


        then: "It should not be accepted"
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("BUDGET_OVERLAPPING"))
    }

    def "Budget term end should not be inside other budget validity period"() {
        given: "Two overlapping budgets"
        def overlap = [
                "data": [
                        "type"      : "budget",
                        "attributes": [
                                "term_beginning" : '2017-01-14',
                                "term_end" : '2017-02-14',
                        ]
                ]
        ]


        when: "Budget is submitted to the system"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(overlap))
                .post("/budget")


        then: "It should not be accepted"
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("BUDGET_OVERLAPPING"))
    }
}
