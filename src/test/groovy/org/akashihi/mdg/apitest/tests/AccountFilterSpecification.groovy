package org.akashihi.mdg.apitest.tests

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.empty
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.not
import static org.junit.Assert.assertThat

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

    def setupSpec() {
        setupAPI();
    }

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
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/account")
        def body = JsonPath.parse(response
                .then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString()
        )
        assertThat(body.read("data[?(@.id == ${accountId})]"), empty())
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
        def response = given()
                .queryParam("filter", "{\"hidden\":true}")
                .contentType("application/vnd.mdg+json").
                when()
                .get("/account")
        def body = JsonPath.parse(response
                .then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString()
        )
        assertThat(body.read("data[?(@.id == ${accountId})]", List.class).size(), is(1))
        assertThat(body.read("data[?(@.id == ${accountId})].attributes.hidden", List.class).first(), is(true))
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
                .queryParam("filter", "{\"name\":\"FilterMe\"}")
                .contentType("application/vnd.mdg+json").
                when()
                .get("/account")

        then: "List should consist only of objects with specified ane"
        def body = JsonPath.parse(response
                .then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString()
        )
        assertThat(body.read("data[?(@.attributes.name == 'FilterMe')]", List.class).size(), is(not(0)))
        assertThat(body.read("data[?(@.id != 'FilterMe')].attributes.hidden", List.class), not(empty()))
    }
}
