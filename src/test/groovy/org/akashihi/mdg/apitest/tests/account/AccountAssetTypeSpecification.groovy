package org.akashihi.mdg.apitest.tests.account

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.API
import org.akashihi.mdg.apitest.fixtures.AccountFixture
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.createSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.errorSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.modifySpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*

class AccountAssetTypeSpecification extends Specification {
    def setupSpec() {
        setupAPI()
    }

    def "User creates new cash account"() {
        when: "Account is submitted to the system"
        def accountId = given().body(JsonOutput.toJson(AccountFixture.assetAccount()))
                .when().post(API.Accounts).
                then().spec(createSpec("/api/account/"))
                .body("data.type", equalTo("account"))
                .body("data.attributes.account_type", equalTo("asset"))
                .body("data.attributes.asset_type", equalTo("cash"))
                .extract().path("data.id")

        then: "Account appears on the accounts list"
        when().get(API.Accounts)
                .then().spec(readSpec())
                .body("data.find {it.id==${accountId}}.attributes.account_type", equalTo("asset"))
                .body("data.find {it.id==${accountId}}.attributes.asset_type", equalTo("cash"))
                .body("data.find {it.id==${accountId}}.attributes.name", equalTo("Wallet"))
    }

    def "User checks asset_type data"() {
        given: "A brand new Account"
        def accountId = AccountFixture.create(AccountFixture.assetAccount())

        when: "Specific Account is requested"
        def response = when().get(API.Account, accountId)

        then: "Account object should be returned"
        response.then().spec(readSpec())
                .body("data.attributes.account_type", equalTo("asset"))
                .body("data.attributes.asset_type", equalTo("cash"))
    }

    def "User modifies asset_type data"() {
        given: "A brand new Account"
        def accountId = AccountFixture.create(AccountFixture.assetAccount())

        when: "New expenseAccount data is submitted"
        def modifiedAccount = AccountFixture.assetAccount()
        modifiedAccount.data.attributes.asset_type = "tradable"
        given().body(JsonOutput.toJson(modifiedAccount))
                .when().put(API.Account, accountId).
                then().spec(modifySpec())
                .body("data.attributes.asset_type", equalTo("tradable"))

        then: "modified data will be retrieved on request"
        when().get(API.Account, accountId)
                .then().spec(readSpec())
                .body("data.attributes.account_type", equalTo("asset"))
                .body("data.attributes.asset_type", equalTo("tradable"))
    }

    def "User can't create non-asset account with asset_type set"() {
        given: "A brand new non-asset Account with asset_type"
        def modifiedAccount = AccountFixture.expenseAccount()
        modifiedAccount.data.attributes.asset_type = 'deposit'

        when: "Account is submitted to the system"
        def response = given().body(JsonOutput.toJson(modifiedAccount))
                .when().post(API.Accounts)

        then: "Account should not be accepted"
        response.then().spec(errorSpec(412, "ACCOUNT_NONASSET_INVALIDFLAG"))
    }

    def "User can't set asset type on non-asset account"() {
        given: "A brand new Account"
        def accountId = AccountFixture.create(AccountFixture.expenseAccount())

        when: "Account data is submitted"
        def modifiedAccount = AccountFixture.expenseAccount()
        modifiedAccount.data.attributes.asset_type = 'deposit'
        def response = given().body(JsonOutput.toJson(modifiedAccount))
                .when().put(API.Account, accountId)
        then: "Account should not be accepted"
        response.then().spec(errorSpec(412, "ACCOUNT_NONASSET_INVALIDFLAG"))
    }

}
