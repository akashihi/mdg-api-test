package org.akashihi.mdg.apitest.tests.setting

import groovy.json.JsonOutput
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.modifySpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*

class UiTransactioDialogCloseSpecification extends Specification {
    static String SETTING_NAME = "ui.transaction.closedialog"

    static def setting = [
            "data": [
                    "type"      : "setting",
                    "id"        : "ui.transaction.closedialog",
                    "attributes": [
                            "value" : "false"
                    ]
            ]
    ]

    def setupSpec() {
        setupAPI()
    }

    def cleanupSpec() {
        def defaultSetting = setting.clone()
        defaultSetting.data.attributes.value = "true"
        given().body(JsonOutput.toJson(defaultSetting))
                .when().put(API.Setting, SETTING_NAME)
                .then().spec(modifySpec())
    }

    def "User modifies close dialog"() {
        given: "A default value true"
        when().get(API.Setting, SETTING_NAME)
        .then().body("data.attributes.value", equalTo("true"))

        when: "New close dialog value is submitted"
        given().body(JsonOutput.toJson(setting))
                .when().put(API.Setting, SETTING_NAME)
                .then().spec(modifySpec())
                .body("data.attributes.value", equalTo("false"))

        then: "modified data will be retrieved on request"
        when()
                .get(API.Setting, SETTING_NAME).
                then().spec(readSpec())
                .body("data.type", equalTo("setting"))
                .body("data.attributes.value", equalTo("false"))

    }
}

