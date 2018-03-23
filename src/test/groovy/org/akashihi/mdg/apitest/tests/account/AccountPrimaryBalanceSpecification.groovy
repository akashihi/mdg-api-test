package org.akashihi.mdg.apitest.tests.account

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class AccountPrimaryBalanceSpecification extends Specification {
    def account = [
            "data": [
                    "type"      : "account",
                    "attributes": [
                            "account_type": "expense",
                            "currency_id" : 978,
                            "name"        : "Rent",
                            "balance"     : 1000
                    ]
            ]
    ]

    def setupSpec() {
        setupAPI()
    }

    def "User creates new account"() {
        given: "A brand new account with initial balance"

        when: "Account is submitted to the system"
        def accountId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(account))
                .post("/account").
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .assertThat().header("Location", containsString("/api/account/"))
                .body("data.type", equalTo("account"))
                .body("data.attributes.balance", equalTo(1000))
                .body("data.attributes.primary_balance", equalTo(1190))
                .extract().path("data.id")

        then: "Account appears on the accounts list"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/account")
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())

        assertThat(body.read("data"), not(empty()))
        assertThat(body.read("data[?(@.id == ${accountId})].type", List.class).first(), equalTo("account"))
        assertThat(body.read("data[?(@.id == ${accountId})].attributes.balance", List.class).first(), equalTo(1000))
        assertThat(body.read("data[?(@.id == ${accountId})].attributes.primary_balance", List.class).first(), equalTo(1190))
    }

    def "User checks account data"() {
        given: "A brand new account with initial balance"
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
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("account"))
                .body("data.attributes.balance", equalTo(1000))
                .body("data.attributes.primary_balance", equalTo(1190))
    }
}
