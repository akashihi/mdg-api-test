package org.akashihi.mdg.apitest.tests

import com.jayway.jsonpath.JsonPath
import spock.lang.*

import static io.restassured.RestAssured.*
import static io.restassured.matcher.RestAssuredMatchers.*
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.akashihi.mdg.apitest.matchers.StringHasSizeMatcher.stringHasSize
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class CurrencySpecification extends Specification {

    def setupSpec() {
        setupAPI();
    }

    def "User requests list of all currencies"() {
        given:
        def request = given()
                .contentType("application/vnd.mdg+json")
        when: "List of currencies is requested"
        def response = request.with().get("/currency")

        then: "Non empty currencies list should be returned"
        def body = JsonPath.parse(response.then()
                .log()
                .ifValidationFails()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())


        assertThat(body.read("data"), not(empty()))
        assertThat(body.read("data[0].type"), equalTo("currency"))
        assertThat(body.read("data[0].id").toString(), stringHasSize(3))
        assertThat(body.read("data[0].attributes.name"), not(stringHasSize(0)))
        assertThat(body.read("data[0].attributes.code"), stringHasSize(3))

        /* 978 is one of the basic predefined currencies
         * and we expect it to be in list.
         */
        assertThat(body.read("data[?(@.id==978)].attributes.code", List.class).first(), is("EUR"))
        assertThat(body.read("data[?(@.id==978)].attributes.name", List.class).first(), is("€"))
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
        def body = JsonPath.parse(response.then().extract().asString())

        assertThat(body.read("data.type"), equalTo("currency"))
        assertThat(body.read("data.id"), equalTo(currencyId))
        assertThat(body.read("data.attributes.code.length"), stringHasSize(3))
    }
}
