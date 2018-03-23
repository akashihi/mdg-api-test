package org.akashihi.mdg.apitest.tests.setting

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat

class CurrencyPrimarySpecification extends Specification {
    def setting = [
            "data": [
                    "type"      : "setting",
                    "id"        : "currency.primary",
                    "attributes": [
                            "value" : "840"
                    ]
            ]
    ]

    def setupSpec() {
        setupAPI();
    }

    def "User lists settings"() {
        given: "A running system"

        when: "Settings lists is retrieved"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/setting")

        then: "Settings list should include primary currency"
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())

        assertThat(body.read("data"), not(empty()))
        assertFalse(body.read("data[?(@.id == 'currency.primary')].type", List.class).isEmpty())
    }

    def "User checks primary currency"() {
        given: "A running system"

        when: "Primary currency setting is requested"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/setting/{id}", "currency.primary")

        then: "Setting object should be returned"
        response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("setting"))
    }

    def "User modifies primary currency"() {
        given: "A running system"

        when: "New primary currency value is submitted"
        given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(setting))
                .put("/setting/{id}", "currency.primary").
                then()
                .assertThat().statusCode(202)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.attributes.value", equalTo("978"))

        then: "modified data will be retrieved on request"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/setting/{id}", "currency.primary").
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("setting"))
                .body("data.attributes.value", equalTo("978"))

    }

    def "Invalid primary currency value is rejected"() {
        given: "A running system"

        when: "Incorrect primary currency value is submitted"
        def invalidSetting = setting.clone()
        invalidSetting.data.attributes.value="-1"
        def response = given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(setting))
                .put("/setting/{id}", "currency.primary")

        then: "Update should be rejected."
        response.then()
                .assertThat().statusCode(422)
                .assertThat().contentType("application/vnd.mdg+json")

    }
}
