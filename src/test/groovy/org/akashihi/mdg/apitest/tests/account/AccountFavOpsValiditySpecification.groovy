package org.akashihi.mdg.apitest.tests.account

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.AccountFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.errorSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI

class AccountFavOpsValiditySpecification extends Specification {
    def setupSpec() {
        setupAPI()
    }

    def "User can't create non-asset account with fav flag set"() {
        given: "A brand new non-asset/fav Account"
        def modifiedAccount = AccountFixture.expenseAccount()
        modifiedAccount.data.attributes.favorite = true

        when: "Account is submitted to the system"
        def response = given().body(JsonOutput.toJson(modifiedAccount))
                .when().post(API.Accounts)

        then: "Account should not be accepted"
        response.then().spec(errorSpec(412, "ACCOUNT_NONASSET_INVALIDFLAG"))
    }

    def "User can't create non-asset account with ops flag set"() {
        given: "A brand new non-asset/fav Account"
        def modifiedAccount = AccountFixture.expenseAccount()
        modifiedAccount.data.attributes.operational = true

        when: "Account is submitted to the system"
        def response = given().body(JsonOutput.toJson(modifiedAccount))
                .when().post(API.Accounts)

        then: "Account should not be accepted"
        response.then().spec(errorSpec(412, "ACCOUNT_NONASSET_INVALIDFLAG"))
    }

    def "User can't set fav flag on non-asset account"() {
        given: "A brand new account"
        def accountId = AccountFixture.create()

        when: "New Account data is submitted"
        def modifiedAccount = AccountFixture.expenseAccount()
        modifiedAccount.data.attributes.favorite = true
        def response = given().body(JsonOutput.toJson(modifiedAccount))
                .when().put(API.Account, accountId)

        then: "Account should not be accepted"
        response.then().spec(errorSpec(412, "ACCOUNT_NONASSET_INVALIDFLAG"))
    }

    def "User can't set ops flag on non-asset account"() {
        given: "A brand new expenseAccount"
        def accountId = AccountFixture.create()

        when: "New Account data is submitted"
        def modifiedAccount = AccountFixture.expenseAccount()
        modifiedAccount.data.attributes.operational = true
        def response = given().body(JsonOutput.toJson(modifiedAccount))
                .when().put(API.Account, accountId)

        then: "Account should not be accepted"
        response.then().spec(errorSpec(412, "ACCOUNT_NONASSET_INVALIDFLAG"))
    }
}
