package org.akashihi.mdg.apitest.fixtures

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given

class BudgetFixture {
    def incomeStateBudget = [
            "data": [
                    "type"      : "budget",
                    "attributes": [
                            "term_beginning" : '2016-12-01',
                            "term_end" : '2016-12-31',
                    ]
            ]
    ]

    def febBudget = [
            "data": [
                    "type"      : "budget",
                    "attributes": [
                            "term_beginning" : '2017-02-13',
                            "term_end" : '2017-02-28',
                    ]
            ]
    ]

    def marBudget = [
            "data": [
                    "type"      : "budget",
                    "attributes": [
                            "term_beginning" : '2017-03-01',
                            "term_end" : '2017-03-31',
                    ]
            ]
    ]

    def aprBudget = [
            "data": [
                    "type"      : "budget",
                    "attributes": [
                            "term_beginning" : '2017-04-01',
                            "term_end" : '2017-04-30',
                    ]
            ]
    ]

    def makeBudget(budget) {
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(budget))
                .post(API.Budgets)
    }

    def removeBudget(id) {
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .delete(API.Budget, id)
                .then()
                .assertThat().statusCode(204)
    }
}
