package org.akashihi.mdg.apitest.tests.account

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class AccountAssetTypeSpecification extends Specification {
    def account = [
            "data": [
                    "type"      : "account",
                    "attributes": [
                            "account_type": "asset",
                            "currency_id" : 978,
                            "name"        : "Waller",
                            "favorite"    : true,
                            "operational" : true,
                            "asset_type"  : 'cash'
                    ]
            ]
    ]

    def nonAsset = [
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
        setupAPI()
    }

    def "User creates new cash account"() {
        given: "A brand new cash account"

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
                .body("data.attributes.asset_type", equalTo("cash"))
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
        assertThat(body.read("data[?(@.id == ${accountId})].attributes.asset_type", List.class).first(), equalTo("cash"))
    }

    def "User checks asset_type data"() {
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
                .body("data.attributes.asset_type", equalTo("cash"))
    }

    def "User modifies asset_type data"() {
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
        modifiedAccount.data.attributes.asset_type = "tradable"
        given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(modifiedAccount))
                .put("/account/{id}", accountId).
                then()
                .assertThat().statusCode(202)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.attributes.asset_type", equalTo("tradable"))

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
                .body("data.attributes.asset_type", equalTo("tradable"))
    }

    def "User can't create non-asset account with asset_type set"() {
        given: "A brand new non-asset account with asset_type"
        def modifiedAccount = nonAsset.clone()
        modifiedAccount.data.attributes.asset_type = 'deposit'

        when: "Account is submitted to the system"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(modifiedAccount))
                .post("/account")

        then: "Account should not be accepted"
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("ACCOUNT_NONASSET_INVALIDFLAG"))
    }

    def "User can't set asset type on non-asset account"() {
        given: "A brand new account"
        def accountId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(nonAsset))
                .post("/account").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        when: "New account data is submitted"
        def modifiedAccount = nonAsset.clone()
        modifiedAccount.data.attributes.asset_type = 'deposit'
        def response = given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(modifiedAccount))
                .put("/account/{id}", accountId)
        then: "Account should not be accepted"
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("ACCOUNT_NONASSET_INVALIDFLAG"))
    }

}
