package org.akashihi.mdg.apitest.tests.category

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.AccountFixture
import org.akashihi.mdg.apitest.fixtures.CategoryFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.errorSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.modifySpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.empty
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.isEmptyOrNullString
import static org.hamcrest.Matchers.not

class CategoryAccountSpecification extends Specification {
    def setupSpec() {
        setupAPI()
    }

    def 'Account is created with the category'() {
        given: 'New category'
        def categoryId = CategoryFixture.create()

        when: 'Account is created with that category'
        def accountId = AccountFixture.create(AccountFixture.expenseAccount(categoryId))

        then: "Account should be linked to the category"
        when().get(API.Account, accountId)
            .then().spec(readSpec())
            .body("data.attributes.category_id", equalTo(categoryId))
    }

    def 'Asset account have default category'() {
        given: 'Current category'
        def categoryId = when().get(API.Categories)
                .then().spec(readSpec())
                .extract().path("data.find {it.attributes.name=='Current'}.id")

        when: 'Asset account is created without category'
        def accountId = AccountFixture.create(AccountFixture.assetAccount())

        then: "Account should be linked to the Current category"
        when().get(API.Account, accountId)
                .then().spec(readSpec())
                .body("data.attributes.category_id", equalTo(categoryId))
    }

    def 'Account is assigned to the category'() {
        given: 'New category and new account'
        def categoryId = CategoryFixture.create()
        def accountId = AccountFixture.create()

        when: 'Account is assigned to that category'
        def modifiedAccount = AccountFixture.expenseAccount()
        modifiedAccount.data.attributes.category_id = categoryId
        given().body(JsonOutput.toJson(modifiedAccount))
                .when().put(API.Account, accountId).
                then().spec(modifySpec())
                .body("data.attributes.category_id", equalTo(categoryId))

        then: "modified data will be retrieved on request"
        when().get(API.Account, accountId)
                .then().spec(readSpec())
                .body("data.attributes.category_id", equalTo(categoryId))
    }

    def 'Category is dropped while begin assigned to account'() {
        given: 'New category and account'
        def categoryId = CategoryFixture.create()
        def accountId = AccountFixture.create(AccountFixture.expenseAccount(categoryId))

        when: "Category is deleted"
        when().delete(API.Category, categoryId)
                .then().statusCode(204)

        then: "Account should be unlinked"
        when().get(API.Account, accountId)
                .then().spec(readSpec())
                .body("data.attributes.category_id", isEmptyOrNullString())
    }

    def 'Category is assigned to the incompatible account'() {
        when: 'Account with incompatible category is created'
        def categoryId = CategoryFixture.create()
        def account = AccountFixture.expenseAccount(categoryId)
        account.data.attributes.account_type="income"

        then: 'Creation should fail'
        given().body(JsonOutput.toJson(account))
                .when().post(API.Accounts)
                .then().spec(errorSpec(412, "CATEGORY_INVALID_TYPE"))
    }
}
