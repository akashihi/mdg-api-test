package org.akashihi.mdg.apitest.tests.account

import org.akashihi.mdg.apitest.fixtures.AccountFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*

class AccountFilterSpecification extends Specification {
    def setupSpec() {
        setupAPI()
    }

    def "User hides an account"() {
        given: "A brand new account"
        def accountId = AccountFixture.create()

        when: "Account is deleted"
        when().delete(API.Account, accountId)
                .then().statusCode(204)

        then: "Deleted expenseAccount still will be available if not filtering is applied"
        when().get(API.Accounts)
            .then().spec(readSpec())
            .body("data.findAll {it.id==${accountId}}", not(empty()))
    }

    def "User requests account list including hidden accounts"() {
        given: "A brand new account"
        def accountId = AccountFixture.create()

        when: "Account is deleted"
        when().delete(API.Account, accountId)
                .then().assertThat().statusCode(204)

        then: "Deleted account should be in accounts list if requested"
        given().queryParam("filter", "{\"hidden\":true}")
                .when().get(API.Accounts)
                .then().spec(readSpec())
                .body("data.findAll {it.id==${accountId}}.size()", is(1))
                .body("data.find {it.id==${accountId}}.attributes.hidden", is(true))
    }

    def "User requests account list excluding hidden accounts"() {
        given: "A brand new account"
        def accountId = AccountFixture.create()

        when: "Account is deleted"
        when().delete(API.Account, accountId)
                .then().assertThat().statusCode(204)

        then: "Deleted account should not be in accounts list as only visible accounts are requested"
        given().queryParam("filter", "{\"hidden\":false}")
                .when().get(API.Accounts)
                .then().spec(readSpec())
                .body("data.findAll {it.id==${accountId}}", empty())
    }

    def "User requests account list, filtering it by some field"() {
        given: "Account with specific name"
        def modifiedAccount = AccountFixture.expenseAccount()
        modifiedAccount.data.attributes.name = "FilterMe"
        AccountFixture.create(modifiedAccount)

        when: "List of accounts with specific name is requested"
        def response = given().queryParam("filter", "{\"name\":\"FilterMe\"}")
                .when().get(API.Accounts)

        then: "List should consist only of objects with specified ane"
        response.then().spec(readSpec())
            .body("data.findAll {it.attributes.name == 'FilterMe'}", not(empty()))
    }
}
