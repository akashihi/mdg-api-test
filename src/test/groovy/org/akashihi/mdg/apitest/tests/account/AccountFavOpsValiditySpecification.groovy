package org.akashihi.mdg.apitest.tests.account

import groovy.json.JsonOutput
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class AccountFavOpsValiditySpecification extends Specification {
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

    def "User can't create non-asset account with fav flag set"() {
        given: "A brand new non-asset/fav account"
        def modifiedAccount = account.clone()
        modifiedAccount.data.attributes.favorite = true

        when: "Account is submitted to the system"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(modifiedAccount))
                .post(API.Accounts)

        then: "Account should not be accepted"
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("ACCOUNT_NONASSET_INVALIDFLAG"))
    }

    def "User can't create non-asset account with ops flag set"() {
        given: "A brand new non-asset/fav account"
        def modifiedAccount = account.clone()
        modifiedAccount.data.attributes.operational = true

        when: "Account is submitted to the system"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(modifiedAccount))
                .post(API.Accounts)

        then: "Account should not be accepted"
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("ACCOUNT_NONASSET_INVALIDFLAG"))
    }

    def "User can't set fav flag on non-asset account"() {
        given: "A brand new account"
        def accountId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(account))
                .post(API.Accounts).
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        when: "New account data is submitted"
        def modifiedAccount = account.clone()
        modifiedAccount.data.attributes.favorite = true
        def response = given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(modifiedAccount))
                .put(API.Account, accountId)
        then: "Account should not be accepted"
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("ACCOUNT_NONASSET_INVALIDFLAG"))
    }

    def "User can't set ops flag on non-asset account"() {
        given: "A brand new account"
        def accountId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(account))
                .post(API.Accounts).
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        when: "New account data is submitted"
        def modifiedAccount = account.clone()
        modifiedAccount.data.attributes.operational = true
        def response = given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(modifiedAccount))
                .put(API.Account, accountId)
        then: "Account should not be accepted"
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("ACCOUNT_NONASSET_INVALIDFLAG"))
    }
}
