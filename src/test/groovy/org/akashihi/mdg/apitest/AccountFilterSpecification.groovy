package org.akashihi.mdg.apitest

import groovy.json.JsonOutput
import spock.lang.*

import static io.restassured.RestAssured.*
import static io.restassured.matcher.RestAssuredMatchers.*
import static org.hamcrest.Matchers.*

class AccountFilterSpecification extends Specification {
    def account = [
            "data": [
                    "type"      : "account",
                    "attributes": [
                            "account_type": "expense",
                            "currency_id" : 978,
                            "name"        : "Rent"
                    ]
            ]
    ]

    def "User hides an account"() {
        given: "A brand new account"
        def accountId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(account))
                .post("/account").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        when: "Account is deleted"
        when()
                .delete("/account/{id}", accountId).
                then()
                .assertThat().statusCode(204)

        then: "Deleted account should be absent from accounts list"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/account").
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data[?(@.id == ${accountId})].length()", is(0))
    }

    def "User requests account list including hidden accounts"() {
        given: "A brand new account"
        def accountId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(account))
                .post("/account").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        when: "Account is deleted"
        when()
                .delete("/account/{id}", accountId).
                then()
                .assertThat().statusCode(204)

        then: "Deleted account should be in accounts list if requested"
        given()
                .queryParam("filter", "{hidden:true}")
                .contentType("application/vnd.mdg+json").
                when()
                .get("/account").
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data[?(@.id == ${accountId})].length()", is(1))
                .body("data[?(@.id == ${accountId})].attributes.hidden", is(true))
    }

    def "User requests account list, filtering it by some field"() {
        def modifiedAccount = account.clone()
        modifiedAccount.data.attributes.name = "FilterMe"
        given: "A brand new account"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(modifiedAccount))
                .post("/account").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        when: "List of accounts with specific name is requested"
        def response = given()
                .queryParam("filter", "{name:FilterMe}")
                .contentType("application/vnd.mdg+json").
                when()
                .get("/account")

        then: "List should consist only of objects with specified ane"
        response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data[?(@.attributes.name == 'FilterMe')].length()", is(not(0)))
                .body("data[?(@.attributes.name != 'FilterMe')].length()", is(0))

    }
}
