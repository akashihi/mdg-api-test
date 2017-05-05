package org.akashihi.mdg.apitest.tests.account

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class AccountFavOpsSpecification extends Specification {
    def account = [
            "data": [
                    "type"      : "account",
                    "attributes": [
                            "account_type": "asset",
                            "currency_id" : 978,
                            "name"        : "Waller",
                            "favorite"    : true,
                            "operational" : true
                    ]
            ]
    ]

    def setupSpec() {
        setupAPI();
    }

    def "User creates new fav/op account"() {
        given: "A brand new fav/op account"

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
                .body("data.attributes.account_type", equalTo("asset"))
                .body("data.attributes.favorite", equalTo(true))
                .body("data.attributes.operational", equalTo(true))
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
        assertThat(body.read("data[?(@.id == ${accountId})].attributes.account_type", List.class).first(), equalTo("asset"))
        assertThat(body.read("data[?(@.id == ${accountId})].attributes.favorite", List.class).first(), equalTo(true))
        assertThat(body.read("data[?(@.id == ${accountId})].attributes.operational", List.class).first(), equalTo(true))
    }

    def "User checks fav/op account data"() {
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
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("account"))
                .body("data.attributes.account_type", equalTo("asset"))
                .body("data.attributes.favorite", equalTo(true))
                .body("data.attributes.operational", equalTo(true))
    }

    def "User modifies fav/op account data"() {
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
        modifiedAccount.data.attributes.favorite = false
        modifiedAccount.data.attributes.operational = false
        given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(modifiedAccount))
                .put("/account/{id}", accountId).
                then()
                .assertThat().statusCode(202)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.attributes.favorite", equalTo(false))
                .body("data.attributes.operational", equalTo(false))

        then: "modified data will be retrieved on request"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/account/{id}", accountId).
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("account"))
                .body("data.attributes.account_type", equalTo("asset"))
                .body("data.attributes.favorite", equalTo(false))
                .body("data.attributes.operational", equalTo(false))
    }

}
