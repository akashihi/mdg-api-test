package org.akashihi.mdg.apitest.tests.transaction

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.*

import java.time.LocalDate
import java.time.LocalDateTime

import static io.restassured.RestAssured.*
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class TransactionFilterSpecification extends Specification {
    TransactionFixture f = new TransactionFixture()

    def setupSpec() {
        setupAPI()
    }

    def "User sorts transaction on timestamp descending"() {
        given: "Several transactions with different dates"
        f.makeTransactions()

        when: "Transaction list sorted by timestamp requested"
        def response = given()
                .queryParam("sort", "timestamp")
                .contentType("application/vnd.mdg+json").
                when()
                .get("/transaction")

        then: "Every timestamp in returned list should be equal or less then previous"
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def ts = body.read("data[*].attributes.timestamp", List.class)
        def current = ts.first()
        ts.each{ x ->
            assertThat(x, lessThanOrEqualTo(current))
            current = x
        }
    }

    def "User requests transactions after specified date"() {
        given: "Several transactions with different dates"
        f.makeTransactions()

        when: "Transaction list filtered using notEarlier"
        def response = given()
                .queryParam("notEarlier", '2017-02-06T00:00:00')
                .contentType("application/vnd.mdg+json").
                when()
                .get("/transaction")

        then: "Every timestamp in returned list should be equal or greater then specified"
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def ts = body.read("data[*].attributes.timestamp", List.class)
        ts.each{ x ->
            assertThat(LocalDateTime.parse(x).toLocalDate(), greaterThanOrEqualTo(LocalDate.parse("2017-02-06")))
        }
    }

    def "User requests transactions before specified date"() {
        given: "Several transactions with different dates"
        f.makeTransactions()

        when: "Transaction list filtered using notLater"
        def response = given()
                .queryParam("notLater", '2017-02-05T00:00:00')
                .contentType("application/vnd.mdg+json").
                when()
                .get("/transaction")

        then: "Every timestamp in returned list should be equal or less then specified"
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def ts = body.read("data[*].attributes.timestamp", List.class)
        ts.each{ x ->
            assertThat(LocalDateTime.parse(x).toLocalDate(), lessThanOrEqualTo(LocalDate.parse("2017-02-04")))
        }
    }

    def "User requests transactions between specified dates"() {
        given: "Several transactions with different dates"
        f.makeTransactions()

        when: "Transaction list filtered using notLater"
        def response = given()
                .queryParam("notEarlier", '2017-02-05T00:00:00')
                .queryParam("notLater", '2017-02-06T00:00:00')
                .contentType("application/vnd.mdg+json").
                when()
                .get("/transaction")

        then: "Every timestamp in returned list should be equal to specified"
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def ts = body.read("data[*].attributes.timestamp", List.class)
        ts.each{ x ->
            assertThat(LocalDateTime.parse(x).toLocalDate(), is(LocalDate.parse("2017-02-05")))
        }
    }


    def "User sorts transaction on timestamp ascending"() {
        given: "Several transactions with different dates"
        f.makeTransactions()

        when: "Transaction list sorted by timestamp requested"
        def response = given()
                .queryParam("sort", "-timestamp")
                .contentType("application/vnd.mdg+json").
                when()
                .get("/transaction")

        then: "Every timestamp in returned list should be equal or less then previous"
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def ts = body.read("data[*].attributes.timestamp", List.class)
        def current = ts.first()
        ts.each{ x ->
            assertThat(x, greaterThanOrEqualTo(current))
            current = x
        }
    }

    def "User filters transaction by tag"() {
        given: "Several transactions with different tags"
        f.makeTransactions()

        when: "Transaction list sorted by timestamp requested"
        def response = given()
                .queryParam("filter", "{\"tag\": [\"transaction\"]} ")
                .contentType("application/vnd.mdg+json").
                when()
                .get("/transaction")

        then: "Should only return transactions, matching filter"
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        assertThat(body.read("data[?(@.attributes.comment == 'Income transaction')]", List.class), not(empty()))
        assertThat(body.read("data[?(@.attributes.comment == 'Test transaction')]", List.class), not(empty()))
        assertThat(body.read("data[?(@.attributes.comment == 'Spend transaction')]", List.class), empty())
    }

    def "User filters transaction by account"() {
        given: "Several accounts and transaction on them"
        def accounts = f.prepareAccounts()
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : '2017-02-05T16:45:36',
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": accounts["income"],
                                                "amount"    : -150
                                        ],
                                        [
                                                "account_id": accounts["asset"],
                                                "amount"    : 50
                                        ],
                                        [
                                                "account_id": accounts["expense"],
                                                "amount"    : 100
                                        ]
                                ]
                        ]
                ]
        ]
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post("/transaction").
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")


        when: "Transaction is filtered by account_id"
        def asset_account_id = accounts["asset"]
        def response = given()
                .queryParam("filter", "{\"account_id\": [${asset_account_id}]} ")
                .contentType("application/vnd.mdg+json").
                when()
                .get("/transaction")

        then: "Should only return transactions, matching filter"
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        assertThat(body.read("data", List.class).size(), is(1)) //As we use new accounts for each test, there should be only one matching transaction
    }
}
