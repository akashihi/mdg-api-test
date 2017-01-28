package org.akashihi.mdg.apitest.tests

import groovy.json.JsonOutput
import spock.lang.*

import static io.restassured.RestAssured.*
import static io.restassured.matcher.RestAssuredMatchers.*
import static org.hamcrest.Matchers.*

class AccountSpecification extends Specification {
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

    def "User creates new account"() {
        given: "A brand new account"

        when: "Account is submitted to the system"
        def accountId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(account))
                .post("/account").
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .assertThat().header("Location", contains("/account/"))
                .body("data.type", equalTo("account"))
                .body("data.attributes.account_type", equalTo("expense"))
                .body("data.attributes.currency_id", equalTo(978))
                .body("data.attributes.name", equalTo("Rent"))
                .extract().path("data.id")

        then: "Account appears on the accounts list"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/account").
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.length()", not(0))
                .body("data[?(@.id == ${accountId})].type", equalTo("account"))
                .body("data[?(@.id == ${accountId})].attributes.account_type", equalTo("expense"))
                .body("data[?(@.id == ${accountId})].attributes.currency_id", equalTo(978))
                .body("data[?(@.id == ${accountId})].attributes.name", equalTo("Rent"))
    }

    def "User checks account data"() {
        given: "A brand new account"
        def accountId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(account))
                .post("/account").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        when: "Specific account is requested"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/account/{id}", accountId)

        then: "Account object should be returned"
        response.then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("account"))
                .body("data.attributes.account_type", equalTo("expense"))
                .body("data.attributes.currency_id", equalTo(978))
                .body("data.attributes.name", equalTo("Rent"))
    }

    def "User modifies account data"() {
        given: "A brand new account"
        def accountId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(account))
                .post("/account").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        when: "New account data is submitted"
        def modifiedAccount = account.clone()
        modifiedAccount.data.attributes.name = "Monthly rent"
        given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(modifiedAccount))
                .put("/account/{id}", accountId).
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.attributes.name", equalTo("Monthly rent"))

        then: "modified data will be retrieved on request"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/account/{id}", accountId).
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("account"))
                .body("data.attributes.account_type", equalTo("expense"))
                .body("data.attributes.currency_id", equalTo(978))
                .body("data.attributes.name", equalTo("Monthly rent"))

    }
}
