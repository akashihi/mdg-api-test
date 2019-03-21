package org.akashihi.mdg.apitest.fixtures

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.createSpec

class BudgetFixture {
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

    static def budget(b, e) {
        return [
                "data": [
                        "type"      : "budget",
                        "attributes": [
                                "term_beginning" : b,
                                "term_end" : e,
                        ]
                ]
        ]
    }

    static def create(budget) {
        given().body(JsonOutput.toJson(budget))
                .when().post(API.Budgets)
                .then().spec(createSpec("/api/budget"))
                .extract().path("data.id")
    }

    static def remove(id) {
                when().delete(API.Budget, id)
                .then().statusCode(204)
    }
}
