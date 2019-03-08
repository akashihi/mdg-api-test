package org.akashihi.mdg.apitest.tests.setting

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.modifySpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat

class UiTransactioDialogCloseSpecification extends Specification {
    String SETTING_NAME = "ui.transaction.closedialog"

    def setting = [
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

    def "User lists settings"() {
        when: "Settings lists is retrieved"
        def response = when().get(API.Settings)

        then: "Settings list should include close dialog"
        response.then().spec(readSpec())
                .body("data", not(empty()))
                .body("data.findAll {it.id=='ui.transaction.closedialog'}.size()", equalTo(1))
    }

    def "User checks close deialog"() {
        when: "Close dialog setting is requested"
        def response = when().get(API.Setting, SETTING_NAME)

        then: "Setting object should be returned"
        response.then().spec(readSpec())
                .body("data.type", equalTo("setting"))
                .body("data.id", equalTo(SETTING_NAME))
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

