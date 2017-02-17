package org.akashihi.mdg.apitest

import static io.restassured.RestAssured.baseURI

/**
 * Configures mdg api connection properties
 */
class apiConnectionBase {
    static Properties properties = new Properties()

    static {
        properties.load(apiConnectionBase.classLoader.getResourceAsStream("api.properties"))
    }

    def static setupAPI() {
        baseURI = properties.baseURI
    }
}
