package org.akashihi.mdg.apitest.tests

import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.*

import static io.restassured.RestAssured.*
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class TransactionFilterSpecification extends Specification {
    TransactionFixture f;

    def "User sorts transaction on timestamp descending"() {
        given: "Several transactions with different dates"
        f.makeTransactions()

        when: "Transaction list sorted by timestamp requested"
        def response = given()
                .pathParam("sort", "timestamp")
                .contentType("application/vnd.mdg+json").
                when()
                .get("/transaction")

        then: "Every timestamp in returned list should be equal or less then previous"
        def ts = response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
        .extract().path("data[*].attributes.timestamp")
        def current = ts.first()
        ts.each{ x ->
            assertThat(x, lessThanOrEqualTo(current))
            current = x
        }
    }

    def "User sorts transaction on timestamp ascending"() {
        given: "Several transactions with different dates"
        f.makeTransactions()

        when: "Transaction list sorted by timestamp requested"
        def response = given()
                .pathParam("sort", "-timestamp")
                .contentType("application/vnd.mdg+json").
                when()
                .get("/transaction")

        then: "Every timestamp in returned list should be equal or less then previous"
        def ts = response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().path("data[*].attributes.timestamp")
        def current = ts.first()
        ts.each{ x ->
            assertThat(x, greaterThanOrEqualTo(current))
            current = x
        }
    }

    def "User filters transaction by comment"() {
        given: "Several transactions with different comments"
        f.makeTransactions()

        when: "Transaction list sorted by timestamp requested"
        def response = given()
                .pathParam("filter", "{'comment' 'income'} ")
                .contentType("application/vnd.mdg+json").
                when()
                .get("/transaction")

        then: "Should only return transactions, matching filter"
        response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data[?(@.attributes.comment == 'Income transaction')].length()", greaterThan(0))
                .body("data[?@.attributes.comment != 'Income transaction')].length()", is(0))
    }

    def "User filters transaction by tag"() {
        given: "Several transactions with different tags"
        f.makeTransactions()

        when: "Transaction list sorted by timestamp requested"
        def response = given()
                .pathParam("filter", "{'tags' ['transaction']} ")
                .contentType("application/vnd.mdg+json").
                when()
                .get("/transaction")

        then: "Should only return transactions, matching filter"
        response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data[?(@.attributes.comment == 'Income transaction')].length()", greaterThan(0))
                .body("data[?(@.attributes.comment == 'Test transaction')].length()", greaterThan(0))
                .body("data[?@.attributes.comment == 'Spend transaction')].length()", is(0))
    }
}
