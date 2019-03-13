package org.akashihi.mdg.apitest.tests.rate

import org.akashihi.mdg.apitest.API
import spock.lang.Specification
import spock.lang.Unroll

import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*

class RateSpecification extends Specification {
    static final Integer EUR = 978
    static final Integer USD = 840
    static final Integer INR = 356
    static final Integer HUF = 348
    def setupSpec() {
        setupAPI()
    }

    def "User lists rates"() {
        when: "Rates are retrieved"
        def response = when().get(API.Rates, "2017-09-20T13:29:00")

        then: "Rate list should include EURUSD ticker"
        response.then().spec(readSpec())
            .body("data", not(empty()))
            .body("data[0].type", is("rate"))
            .body("data[0].attributes.from_currency", is(EUR))
            .body("data[0].attributes.to_currency", is(USD))
            .body("data[0].attributes.rate.toString()", is("1.19"))
    }

    @Unroll
    def "User checks #RATENAME rate"() {
        expect:
        when().get(API.Rate, "2017-09-20T13:29:00", F, T)
        .then().log().all().spec(readSpec())
                .body("data.type", is("rate"))
                .body("data.attributes.from_currency", is(F))
                .body("data.attributes.to_currency", is(T))
                .body("data.attributes.rate.toString()", is(V))

        where:
        RATENAME      | F | T | V
        "existing"    | EUR | USD | "1.19"
        "nonexisting" | INR | HUF | "1"
    }
}
