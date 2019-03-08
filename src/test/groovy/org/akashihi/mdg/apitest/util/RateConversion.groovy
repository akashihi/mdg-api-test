package org.akashihi.mdg.apitest.util

import com.jayway.jsonpath.JsonPath
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given

class RateConversion {
    static def primaryCurrency
    static def rateMap = [:]

    def RateConversion() {
        primaryCurrency = JsonPath.parse(given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Setting, "currency.primary")
                .then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString()).read("data.attributes.value", Long.class)
    }

    public applyRate(value, currency) {
        if ( !rateMap.containsKey(currency)) {
            def rate = JsonPath.parse(given()
                    .contentType("application/vnd.mdg+json").
                    when()
                    .get(API.Rate, "2017-04-01T13:29:00", currency, primaryCurrency)
                    .then()
                    .assertThat().statusCode(200)
                    .assertThat().contentType("application/vnd.mdg+json")
                    .extract().asString())
                    .read("data.attributes.rate", BigDecimal.class)
            rateMap[currency]=rate
        }

        return value.multiply(rateMap[currency])
    }

    public getCurrencyForAccount(account_id) {
        return JsonPath.parse(given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Accounts)
                .then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString()).read("data[?(@.id == ${account_id})].attributes.currency_id", List.class).first()

    }

}
