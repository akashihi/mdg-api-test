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

    def "User creates new transaction"() {
        given: "Brand new febBudget"

        when: "New transaction is submitted to the system"
        def budget = [
                "data": [
                        "type"      : "buget",
                        "attributes": [
                                "term_beginning" : '2017-02-05',
                                "term_end" : '2017-02-05',
                        ]
                ]
        ]
        def response =given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(budget))
                .post("/febBudget")
        def body = JsonPath.parse(response.
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .assertThat().header("Location", containsString("/api/febBudget/20170205"))
                .extract().asString())

        assertThat(body.read("data.type"), equalTo("febBudget"))
        assertThat(body.read("data.attributes.term_beginning"), equalTo("2017-02-05"))
        assertThat(body.read("data.attributes.term_end"), equalTo("2017-02-06"))
        def bId =response.then().extract().path("data.id")

        then: "Budget appears on febBudget list"
        def listResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget")
        def listBody =  JsonPath.parse(listResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        assertThat(listBody.read("data", List.class).size(), is(not(0)))
        assertThat(listBody.read("data[?(@.id == ${bId})].type", List.class).first(), equalTo("febBudget"))
        assertThat(listBody.read("data[?(@.id == ${bId})].attributes.term_beginning", List.class).first(), equalTo("2017-02-05"))
        assertThat(listBody.read("data[?(@.id == ${bId})].attributes.term_end", List.class).first(), equalTo("2017-02-06"))
    }

    def "User checks budget data"() {
        given: "Brand new febBudget"
        def budget = [
                "data": [
                        "type"      : "buget",
                        "attributes": [
                                "term_beginning" : '2017-02-07',
                                "term_end" : '2017-02-08',
                        ]
                ]
        ]

        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(budget))
                .post("/febBudget")

        when: "Budget is requested by specifying it's data"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/febBudget/{id}", "20170208")


        then: "Budget data should be returned"
        response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("febBudget"))
                .body("data.attributes.term_beginning", equalTo("2017-02-07"))
                .body("data.attributes.term_end", equalTo("2017-02-08"))
    }

    def "User deletes budget"() {
        given: "Brand new febBudget"
        def budget = [
                "data": [
                        "type"      : "buget",
                        "attributes": [
                                "term_beginning" : '2017-02-09',
                                "term_end" : '2017-02-10',
                        ]
                ]
        ]

        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(budget))
                .post("/febBudget")

        when: "Budget is deleted"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .delete("/febBudget/{id}", "20170209")
                .then()
                .assertThat().statusCode(204)


        then: "it should disappear from febBudget list"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/febBudget")
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())

        assertThat(body.read("data[?(@.id == '20170509)]", List.class), empty())
    }
}
