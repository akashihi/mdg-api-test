package org.akashihi.mdg.apitest.tests.currency

import groovy.json.JsonOutput
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.errorSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.modifySpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.akashihi.mdg.apitest.matchers.StringHasSizeMatcher.stringHasSize
import static org.hamcrest.Matchers.*

class CurrencySpecification extends Specification {
    static Integer CURRENCY_ID = 978

    def setupSpec() {
        setupAPI()
    }

    def "User requests list of all currencies"() {
        when: "List of currencies is requested"
        def response = when().get(API.Currencies)

        then: "Non empty currencies list should be returned"
        response.then().spec(readSpec())
                .body("data", not(empty()))
                .body("data.getAt(0).type", equalTo("currency"))
                .body("data.getAt(0).id.toString()", stringHasSize(3))
                .body("data.getAt(0).attributes.name", not(isEmptyString()))
                .body("data.getAt(0).attributes.code", stringHasSize(3))
    }

    def "User requests specific currency object"() {
        /* 978 is one of the basic predefined currencies
         * and we expect it to be in list.
         */
        when: "Specific currency is requested"
        def response = when().get(API.Currency, CURRENCY_ID)

        then: "Currency object should be returned"
        response.then().spec(readSpec())
                .body("data.type", equalTo("currency"))
                .body("data.id", equalTo(CURRENCY_ID))
                .body("data.attributes.code", is("EUR"))
                .body("data.attributes.name", is("€"))
    }

    def "User requests non-existent currency object"() {
        /* By definition all currency ids
         * are in between 100 and 999 inclusive
         */
        given: "Some invalid currency id"
        def currencyId = 1

        when: "Specific currency is requested"
        def response = when().get(API.Currency, currencyId)

        then: "Not found error should be returned"
        response.then().spec(errorSpec(404, "CURRENCY_NOT_FOUND"))
    }

    def "User disables currency"() {
        given: "A currency object"
        def currencyId =when().get(API.Currencies)
                .then().spec(readSpec())
                .extract().path("data[0].id")

        and: "Specific currency is requested"
        def currency = when().get(API.Currency, currencyId)
                .then().spec(readSpec())
                .extract().jsonPath().getMap("")


        when: "New currency data is submitted"
        currency.data.attributes.active = false
        given().body(JsonOutput.toJson(currency))
                .when().put(API.Currency, currencyId).
                then().spec(modifySpec())
                .body("data.attributes.active", is(false))

        then: "modified data will be retrieved on request"
        when().get(API.Currency, currencyId)
                .then().spec(readSpec())
                .body("data.attributes.active", is(false))
    }

}
