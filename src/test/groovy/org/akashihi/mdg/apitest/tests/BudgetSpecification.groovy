package org.akashihi.mdg.apitest.tests

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class BudgetSpecification extends Specification {
    def setupSpec() {
        setupAPI();
    }

    def "User creates new budget"() {
        given: "Brand new budget"

        when: "New budget is submitted to the system"
        def budget = [
                "data": [
                        "type"      : "buget",
                        "attributes": [
                                "term_beginning" : '2017-02-04',
                                "term_end" : '2017-02-06',
                        ]
                ]
        ]
        def response =given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(budget))
                .post("/budget")
        def body = JsonPath.parse(response.
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .assertThat().header("Location", containsString("/api/budget/20170204"))
                .extract().asString())

        assertThat(body.read("data.type"), equalTo("budget"))
        assertThat(body.read("data.attributes.term_beginning"), equalTo("2017-02-04"))
        assertThat(body.read("data.attributes.term_end"), equalTo("2017-02-06"))
        def bId =response.then().extract().path("data.id")

        then: "Budget appears on budget list"
        def listResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget")
        def listBody =  JsonPath.parse(listResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        assertThat(listBody.read("data", List.class).size(), is(not(0)))
        assertThat(listBody.read("data[?(@.id == ${bId})].type", List.class).first(), equalTo("budget"))
        assertThat(listBody.read("data[?(@.id == ${bId})].attributes.term_beginning", List.class).first(), equalTo("2017-02-04"))
        assertThat(listBody.read("data[?(@.id == ${bId})].attributes.term_end", List.class).first(), equalTo("2017-02-06"))
    }

    def "User checks budget data"() {
        given: "Brand new budget"
        def budget = [
                "data": [
                        "type"      : "budget",
                        "attributes": [
                                "term_beginning" : '2017-02-07',
                                "term_end" : '2017-02-09',
                        ]
                ]
        ]

        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(budget))
                .post("/budget")

        when: "Budget is requested by specifying it's data"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/{id}", "20170208")


        then: "Budget data should be returned"
        response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("budget"))
                .body("data.attributes.term_beginning", equalTo("2017-02-07"))
                .body("data.attributes.term_end", equalTo("2017-02-09"))
    }

    def "User deletes budget"() {
        given: "Brand new febBudget"
        def budget = [
                "data": [
                        "type"      : "buget",
                        "attributes": [
                                "term_beginning" : '2017-02-10',
                                "term_end" : '2017-02-12',
                        ]
                ]
        ]

        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(budget))
                .post("/budget")

        when: "Budget is deleted"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .delete("/budget/{id}", "20170210")
                .then()
                .assertThat().statusCode(204)


        then: "it should disappear from budget list"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget")
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())

        assertThat(body.read("data[?(@.id == '20170210')]", List.class), empty())
    }
}
