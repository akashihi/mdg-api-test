package org.akashihi.mdg.apitest.tests.setting

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat

class UiTransactioDialogCloseSpecification extends Specification {
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
        given: "A running system"

        when: "Settings lists is retrieved"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/setting")

        then: "Settings list should include close dialog"
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())

        assertThat(body.read("data"), not(empty()))
        assertFalse(body.read("data[?(@.id == 'ui.transaction.closedialog')].type", List.class).isEmpty())
    }

    def "User checks close deialog"() {
        given: "A running system"

        when: "Close dialog setting is requested"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/setting/{id}", "ui.transaction.closedialog")

        then: "Setting object should be returned"
        response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("setting"))
    }

    def "User modifies close dialog"() {
        given: "A running system"

        when: "New close dialog value is submitted"
        given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(setting))
                .put("/setting/{id}", "ui.transaction.closedialog").
                then()
                .assertThat().statusCode(202)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.attributes.value", equalTo("false"))

        then: "modified data will be retrieved on request"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/setting/{id}", "ui.transaction.closedialog").
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("setting"))
                .body("data.attributes.value", equalTo("false"))

    }
}

