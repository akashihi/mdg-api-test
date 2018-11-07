package org.akashihi.mdg.apitest.fixtures

import groovy.json.JsonOutput

import java.time.LocalDateTime

import static io.restassured.RestAssured.given

class TotalsReportFixture {
    def accountAsset = [
            "data": [
                    "type"      : "account",
                    "attributes": [
                            "account_type": "asset",
                            "currency_id" : 978,
                            "name"        : "Current",
                            "asset_type"  : "broker"
                    ]
            ]
    ]

    def accountIncome = [
            "data": [
                    "type"      : "account",
                    "attributes": [
                            "account_type": "income",
                            "currency_id" : 840,
                            "name"        : "Salary"
                    ]
            ]
    ]

    def accountUsdAsset = [
            "data": [
                    "type"      : "account",
                    "attributes": [
                            "account_type": "asset",
                            "currency_id" : 840,
                            "name"        : "USD current",
                            "asset_type"  : "broker"
                    ]
            ]
    ]

    private static def makeAccount(account) {
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(account))
                .post("/account").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")
    }

    def prepareAccounts() {
        [
                "income": Long.valueOf(makeAccount(accountIncome)),
                "asset": Long.valueOf(makeAccount(accountAsset)),
                "usdAsset": Long.valueOf(makeAccount(accountUsdAsset)),
        ]
    }

    def makeTransaction(transaction) {
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post("/transaction").
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().path("data.id")
    }
}
