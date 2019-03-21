package org.akashihi.mdg.apitest.util

import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec

class RateConversion {
    def primaryCurrency
    def rateMap = [:]

    RateConversion() {
        primaryCurrency = when().get(API.Setting, "currency.primary")
            .then().spec(readSpec())
            .extract().path("data.attributes.value")
    }

    def applyRate(value, currency) {
        if ( !rateMap.containsKey(currency)) {
            BigDecimal rate = when().get(API.Rate, "2017-04-01T13:29:00", currency, primaryCurrency)
                    .then().spec(readSpec())
                    .extract().path("data.attributes.rate")
            rateMap[currency]=rate
        }

        return value.multiply(rateMap[currency])
    }

    static getCurrencyForAccount(account_id) {
        when().get(API.Accounts)
                .then().spec(readSpec())
                .extract().path("data.find {it.id==${account_id}}.attributes.currency_id")
    }
}
