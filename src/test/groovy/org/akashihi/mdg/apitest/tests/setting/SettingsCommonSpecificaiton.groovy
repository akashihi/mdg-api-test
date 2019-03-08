package org.akashihi.mdg.apitest.tests.setting

import org.akashihi.mdg.apitest.API
import spock.lang.Specification
import spock.lang.Unroll

import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.empty
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.not

class SettingsCommonSpecificaiton extends Specification {
    def setupSpec() {
        setupAPI()
    }

    @Unroll
    def 'User lists settings and looks for #SETTING_NAME'() {
        expect:
        when().get(API.Settings)
            .then().spec(readSpec())
            .body("data", not(empty()))
            .body("data.findAll {it.id=='${SETTING_NAME}'}.size()", equalTo(1))

        where:
        SETTING_NAME | _
        "currency.primary" | _
        "ui.transaction.closedialog" | _
    }

    @Unroll
    def "User checks setting #SETTING_NAME"() {
        expect: "Primary currency setting is requested"
        when().get(API.Setting, SETTING_NAME)
            .then().spec(readSpec())
            .body("data.type", equalTo("setting"))
            .body("data.id", equalTo(SETTING_NAME))

        where:
        SETTING_NAME | _
        "currency.primary" | _
        "ui.transaction.closedialog" | _
    }

}
