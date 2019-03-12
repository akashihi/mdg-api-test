package org.akashihi.mdg.apitest

import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.specification.RequestSpecification

import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalTo

/**
 * Configures mdg api connection properties
 */
class apiConnectionBase {
    static Properties properties = new Properties()

    static {
        properties.load(apiConnectionBase.classLoader.getResourceAsStream("api.properties"))
    }

    def static setupAPI() {
        RequestSpecification baseRequest = new RequestSpecBuilder()
                .setBaseUri(properties.baseURI)
                .build()

        RestAssured.requestSpecification = baseRequest
    }

    def static readSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType("application/vnd.mdg+json")
                .build()
    }

    def static createSpec(String location) {
        return new ResponseSpecBuilder()
                .expectStatusCode(201)
                .expectContentType("application/vnd.mdg+json")
                .expectHeader("Location", containsString(location))
                .build()
    }

    def static modifySpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(202)
                .expectContentType("application/vnd.mdg+json")
                .build()
    }

    def static errorSpec(Integer errorCode, String errorMsg) {
        return new ResponseSpecBuilder()
                .expectStatusCode(errorCode)
                .expectContentType("application/vnd.mdg+json")
                .expectBody("errors[0].code", equalTo(errorMsg))
                .build()
    }
}
