package org.akashihi.mdg.apitest.tests.account


import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.AccountFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.createSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.modifySpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.equalTo

class AccountSpecification extends Specification {
    def setupSpec() {
        setupAPI()
    }

    def "User creates new account"() {
        when: "Account is submitted to the system"
        def accountId = given().body(JsonOutput.toJson(AccountFixture.expenseAccount()))
                .when().post(API.Accounts)
                .then().spec(createSpec("/api/account"))
                .body("data.type", equalTo("account"))
                .body("data.attributes.account_type", equalTo("expense"))
                .body("data.attributes.currency_id", equalTo(978))
                .body("data.attributes.name", equalTo("Rent"))
                .extract().path("data.id")

        then: "Account appears on the accounts list"
        when().get(API.Accounts)
                .then().spec(readSpec())
                .body("data.find {it.id==${accountId}}.type", equalTo("account"))
                .body("data.find {it.id==${accountId}}.attributes.account_type", equalTo("expense"))
                .body("data.find {it.id==${accountId}}.attributes.currency_id", equalTo(978))
                .body("data.find {it.id==${accountId}}.attributes.name", equalTo("Rent"))
    }

    def "User checks account data"() {
        given: "A brand new expenseAccount"
        def accountId = AccountFixture.create(AccountFixture.expenseAccount())

        when: "Specific expenseAccount is requested"
        def response =when().get(API.Account, accountId)

        then: "Account object should be returned"
        response.then().spec(readSpec())
                .body("data.type", equalTo("account"))
                .body("data.attributes.account_type", equalTo("expense"))
                .body("data.attributes.currency_id", equalTo(978))
                .body("data.attributes.name", equalTo("Rent"))
    }

    def "User modifies account data"() {
        given: "A brand new expenseAccount"
        def accountId = AccountFixture.create(AccountFixture.expenseAccount())

        when: "New expenseAccount data is submitted"
        def modifiedAccount = AccountFixture.expenseAccount()
        modifiedAccount.data.attributes.name = "Monthly rent"
        given().body(JsonOutput.toJson(modifiedAccount))
                .when().put(API.Account, accountId)
                .then().spec(modifySpec())
                .body("data.attributes.name", equalTo("Monthly rent"))

        then: "modified data will be retrieved on request"
                when().get(API.Account, accountId).
                then().spec(readSpec())
                .body("data.attributes.name", equalTo("Monthly rent"))
    }
}
