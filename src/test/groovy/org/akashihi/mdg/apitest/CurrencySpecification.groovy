package org.akashihi.mdg.apitest

import spock.lang.*

import static io.restassured.RestAssured.*
import static io.restassured.matcher.RestAssuredMatchers.*
import static org.hamcrest.Matchers.*

class CurrencySpecification extends Specification {

    def "User requests list of all currencies"() {
        given:
        def request = given()
                .contentType("application/vnd.mdg+json")
        when: "List of currencies is requested"
        def response = request.with().get("/currency")

        then: "Non empty currencies list should be returned"
        response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.length()", not(0))
                .body("data[0].type", equalTo("currency"))
                .body("data[0].id.length()", is(3))
                .body("data[0].attributes.name.length()", not(0))
                .body("data[0].attributes.code.length()", is(3))
        /* 978 is one of the basic predefined currencies
         * and we expect it to be in list.
         */
                .body("data[?(@.id == 978)].attributes.code", is("EUR"))
                .body("data[?(@.id == 978)].attributes.name", is("€"))
    }

    def "User requests specific currency object"() {
        given: "User have currencies list"
        def currencyId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/currency").
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().path("data[0].id")

        when: "Specific currency is requested"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/currency/{id}", currencyId)

        then: "Currency object should be returned"
        response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", is("currency"))
                .body("data.id", equalTo(currencyId))
                .body("data.attributes.name.length()", is(3))
                .body("data.name.length()", is(3))

    }
}
