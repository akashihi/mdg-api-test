package org.akashihi.mdg.apitest.tests.account

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.AccountFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.createSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*

class AccountFavOpsSpecification extends Specification {
    def setupSpec() {
        setupAPI()
    }

    def "User creates new fav/op account"() {
        when: "Account is submitted to the system"
        def accountId = given().body(JsonOutput.toJson(AccountFixture.assetAccount()))
                .when().post(API.Accounts).
                then().spec(createSpec("/api/account"))
                .body("data.attributes.account_type", equalTo("asset"))
                .body("data.attributes.favorite", equalTo(true))
                .body("data.attributes.operational", equalTo(true))
                .extract().path("data.id")

        then: "Account appears on the accounts list"
        when().get(API.Accounts)
                .then().spec(readSpec())
                .body("data.find {it.id==${accountId}}.attributes.account_type", equalTo("asset"))
                .body("data.find {it.id==${accountId}}.attributes.favorite", is(true))
                .body("data.find {it.id==${accountId}}.attributes.operational", is(true))
    }

    def "User checks fav/op account data"() {
        given: "A brand new Account"
        def accountId = AccountFixture.create(AccountFixture.assetAccount())

        when: "Specific expenseAccount is requested"
        def response = when().get(API.Account, accountId)

        then: "Account object should be returned"
        response.then().spec(readSpec())
                .body("data.attributes.account_type", equalTo("asset"))
                .body("data.attributes.favorite", equalTo(true))
                .body("data.attributes.operational", equalTo(true))
    }

    def "User modifies fav/op account data"() {
        given: "A brand new Account"
        def accountId = AccountFixture.create(AccountFixture.assetAccount())

        when: "New expenseAccount data is submitted"
        def modifiedAccount = AccountFixture.assetAccount()
        modifiedAccount.data.attributes.favorite = false
        modifiedAccount.data.attributes.operational = false
        given().body(JsonOutput.toJson(modifiedAccount))
                .when().put(API.Account, accountId)
                .then()
                .body("data.attributes.favorite", equalTo(false))
                .body("data.attributes.operational", equalTo(false))

        then: "modified data will be retrieved on request"
                when().get(API.Account, accountId)
                .then().spec(readSpec())
                .body("data.attributes.favorite", equalTo(false))
                .body("data.attributes.operational", equalTo(false))
    }
}
