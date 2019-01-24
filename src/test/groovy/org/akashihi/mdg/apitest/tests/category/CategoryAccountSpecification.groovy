package org.akashihi.mdg.apitest.tests.category

import groovy.json.JsonOutput
import spock.lang.Specification

class CategoryAccountSpecification extends Specification {
    def category = [
            "data": [
                    "type"      : "category",
                    "attributes": [
                            "name": "Bonuses",
                            "order" : 1,
                            "account_type" : "expense"
                    ]
            ]
    ]

    def setupSpec() {
        setupAPI()
    }

    def 'Account is created with the category'() {
        given 'New category'
        def categoryId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(category))
                .post("/category").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        when 'Account is created with that category'
        def account = [
                "data": [
                        "type"      : "account",
                        "attributes": [
                                "account_type": "expense",
                                "currency_id" : 978,
                                "name"        : "Rent",
                                "category_id" : categoryId
                        ]
                ]
        ]
        def accountId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(account))
                .post("/account").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        then: "Account should be linked to the category"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/account/{id}", accountId)

        response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("account"))
                .body("data.attributes.category_id", equalTo(categoryId))
    }

    def 'Account is assigned to the category'() {
        given 'New category and new account'
        def categoryId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(category))
                .post("/category").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")
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
        def accountId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(account))
                .post("/account").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        when 'Account is assigned to that category'
        def modifiedAccount = account.clone()
        modifiedAccount.data.attributes.category_id = categoryId
        given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(modifiedAccount))
                .put("/account/{id}", accountId).
                then()
                .assertThat().statusCode(202)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.attributes.category_id", equalTo(categoryId))

        then: "modified data will be retrieved on request"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/account/{id}", accountId).
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("account"))
                .body("data.attributes.category_id", equalTo(categoryId))
    }

    def 'Category is dropped while begin assigned to account'() {
        given 'New category and account'
        def categoryId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(category))
                .post("/category").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")
        def account = [
                "data": [
                        "type"      : "account",
                        "attributes": [
                                "account_type": "expense",
                                "currency_id" : 978,
                                "name"        : "Rent",
                                "category_id" : categoryId
                        ]
                ]
        ]
        def accountId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(account))
                .post("/account").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        when: "Category is deleted"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .delete("/category/{id}", categoryId)
                .then()
                .assertThat().statusCode(204)

        then: "Account should be unlinked"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/account/{id}", accountId)

        response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("account"))
                .body("data.attributes.category_id", empty())
    }

    def 'Category is assigned to the incompatible account'() {
        given 'New category and account'
        def categoryId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(category))
                .post("/category").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        when 'Account with incompatible category is created'
        def account = [
                "data": [
                        "type"      : "account",
                        "attributes": [
                                "account_type": "income",
                                "currency_id" : 978,
                                "name"        : "Rent",
                                "category_id" : categoryId
                        ]
                ]
        ]
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(account))
                .post("/account")
        then: 'Creation is declined'
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("CATEGORY_INVALID_TYPE"))
    }
}
