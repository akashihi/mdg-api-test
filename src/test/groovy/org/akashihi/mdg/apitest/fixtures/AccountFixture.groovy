package org.akashihi.mdg.apitest.fixtures

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.createSpec

class AccountFixture {
    static def expenseAccount(Long category_id = null) {
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

    static def assetAccount(Long category_id = null) {
        return [
                "data": [
                        "type"      : "account",
                        "attributes": [
                                "account_type": "asset",
                                "currency_id" : 978,
                                "name"        : "Wallet",
                                "favorite"    : true,
                                "operational" : true,
                                "asset_type"  : 'cash',
                                "category_id" : category_id
                        ]
                ]
        ]
    }

    static def incomeAccount(Long category_id = null) {
        return [
                "data": [
                        "type"      : "account",
                        "attributes": [
                                "account_type": "income",
                                "currency_id" : 978,
                                "name"        : "Salary",
                                "category_id" : category_id
                        ]
                ]
        ]
    }

    static def usdAccount(Long category_id = null) {
        return [
                "data": [
                        "type"      : "account",
                        "attributes": [
                                "account_type": "asset",
                                "currency_id" : 840,
                                "name"        : "USD",
                                "category_id" : category_id
                        ]
                ]
        ]
    }

    static def create() {
        create(expenseAccount())
    }

    static def create(account) {
        return given().body(JsonOutput.toJson(account))
                .when()
                .post(API.Accounts)
                .then().spec(createSpec("/api/account"))
                .extract().path("data.id")
    }
}
