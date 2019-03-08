package org.akashihi.mdg.apitest.tests.rate

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.API
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat

class RateSpecification extends Specification {
    def setting = [
            "data": [
                    "type"      : "setting",
                    "id"        : "currency.primary",
                    "attributes": [
                            "value" : "978"
                    ]
            ]
    ]

    def setupSpec() {
        setupAPI();
    }

    def "User lists rates"() {
        given: "A running system"

        when: "Settings lists is retrieved"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Rates, "2017-09-20T13:29:00")

        then: "Rate list should include EURUSD ticker"
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())

        assertThat(body.read("data", List.class), not(empty()))
        assertThat(body.read("data[0].type"), is("rate"))
        assertThat(body.read("data[0].attributes.from_currency"), is(978))
        assertThat(body.read("data[0].attributes.to_currency"), is(840))
        assertThat(body.read("data[0].attributes.rate", String.class), is("1.19"))
    }

    def "User checks rate"() {
        given: "A running system"

        when: "EURUSD rate is requested"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Rate, "2017-09-20T13:29:00", 978, 840)

        then: "Rate object should be returned"
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString());

        assertThat(body.read("data.type"), is("rate"))
        assertThat(body.read("data.attributes.from_currency"), is(978))
        assertThat(body.read("data.attributes.to_currency"), is(840))
        assertThat(body.read("data.attributes.rate", String.class), is("1.19"))
    }

    def "User checks for non existing rate"() {
        given: "A running system"

        when: "INRHUF rate is requested"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Rate, "2017-09-20T13:29:00", 356, 348)

        then: "Rate object should be returned"
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString());

        assertThat(body.read("data.type"), is("rate"))
        assertThat(body.read("data.attributes.from_currency"), is(356))
        assertThat(body.read("data.attributes.to_currency"), is(348))
        assertThat(body.read("data.attributes.rate"), is(1))
    }
}
