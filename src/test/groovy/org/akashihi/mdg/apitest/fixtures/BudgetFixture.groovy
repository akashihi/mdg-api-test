package org.akashihi.mdg.apitest.fixtures

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.createSpec

class BudgetFixture {
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
