package org.akashihi.mdg.apitest.fixtures

import groovy.json.JsonOutput

import static io.restassured.RestAssured.given

class BudgetFixture {
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

    def makeBudget(budget) {
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(budget))
                .post("/budget")
    }

    def removeBudget(id) {
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .delete("/budget/{id}", id)
                .then()
                .assertThat().statusCode(204)
    }
}
