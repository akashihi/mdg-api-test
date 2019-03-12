package org.akashihi.mdg.apitest.fixtures

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.createSpec

class AccountFixture {
    static def account(Long category_id = null) {
        return [
                "data": [
                        "type"      : "account",
                        "attributes": [
                                "account_type": "expense",
                                "currency_id" : 978,
                                "name"        : "Rent",
                                "category_id" : category_id
                        ]
                ]
        ]
    }

    static def create() {
        create(account())
    }

    static def create(account) {
        return given().body(JsonOutput.toJson(account))
                .when()
                .post(API.Accounts)
                .then().spec(createSpec("/api/account"))
                .extract().path("data.id")
    }
}
