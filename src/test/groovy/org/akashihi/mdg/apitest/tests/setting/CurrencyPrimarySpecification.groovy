package org.akashihi.mdg.apitest.tests.setting

import groovy.json.JsonOutput
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.errorSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.modifySpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*

class CurrencyPrimarySpecification extends Specification {
    static String SETTING_NAME = "currency.primary"

    static def setting = [
            "data": [
                    "type"      : "setting",
                    "id"        : "currency.primary",
                    "attributes": [
                            "value" : "978"
                    ]
            ]
    ]

    def setupSpec() {
        setupAPI()
    }

    def cleanupSpec() {
        def defaultSetting = setting.clone()
        defaultSetting.data.attributes.value = "840"
        given().body(JsonOutput.toJson(defaultSetting))
                .when().put(API.Setting, SETTING_NAME)
                .then().spec(modifySpec())
    }

    def "User modifies primary currency"() {
        given: "A default value true"
                when().get(API.Setting, SETTING_NAME).
                then().spec(readSpec())
                .body("data.attributes.value", equalTo("840"))

        when: "New primary currency value is submitted"
        given().body(JsonOutput.toJson(setting))
                .when().put(API.Setting, SETTING_NAME).
                then().spec(modifySpec())
                .body("data.attributes.value", equalTo("978"))

        then: "modified data will be retrieved on request"
                when().get(API.Setting, SETTING_NAME).
                then()
                .body("data.type", equalTo("setting"))
                .body("data.id", equalTo(SETTING_NAME))
                .body("data.attributes.value", equalTo("978"))
    }

    def "Invalid primary currency value is rejected"() {
        given: "A running system"

        when: "Incorrect primary currency value is submitted"
        def invalidSetting = setting.clone()
        invalidSetting.data.attributes.value="-1"

        then: "Update should be rejected."
        given().body(JsonOutput.toJson(setting))
                .when().put(API.Setting, SETTING_NAME)
                .then().spec(errorSpec(422, "SETTING_DATA_INVALID"))

    }
}
