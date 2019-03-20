package org.akashihi.mdg.apitest.tests.transaction

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.AccountFixture
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import org.akashihi.mdg.apitest.API
import spock.lang.*

import java.time.LocalDate
import java.time.LocalDateTime

import static io.restassured.RestAssured.*
import static org.akashihi.mdg.apitest.apiConnectionBase.createSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class TransactionFilterSpecification extends Specification {
    def setupSpec() {
        setupAPI()
    }

    def "User sorts transaction on timestamp descending"() {
        given: "Several transactions with different dates"
        TransactionFixture.createMultiple()

        when: "Transaction list sorted by timestamp requested"
        def response = given()
                .queryParam("sort", "timestamp")
                .when().get(API.Transactions)

        then: "Every timestamp in returned list should be equal or less then previous"
        def ts = response.then().spec(readSpec())
            .extract().path("data.findAll().attributes.timestamp")
        def current = ts.first()
        ts.each{ x ->
            assertThat(x, lessThanOrEqualTo(current))
            current = x
        }
    }

    def "User requests transactions after specified date"() {
        given: "Several transactions with different dates"
        TransactionFixture.createMultiple()

        when: "Transaction list filtered using notEarlier"
        def response = given()
                .queryParam("notEarlier", '2017-02-06T00:00:00')
                .when().get(API.Transactions)

        then: "Every timestamp in returned list should be equal or greater then specified"
        def ts = response.then().spec(readSpec())
                .extract().path("data.findAll().attributes.timestamp")
        ts.each{ x ->
            assertThat(LocalDateTime.parse(x).toLocalDate(), greaterThanOrEqualTo(LocalDate.parse("2017-02-06")))
        }
    }

    def "User requests transactions before specified date"() {
        given: "Several transactions with different dates"
        TransactionFixture.createMultiple()

        when: "Transaction list filtered using notLater"
        def response = given()
                .queryParam("notLater", '2017-02-05T00:00:00')
                .when().get(API.Transactions)

        then: "Every timestamp in returned list should be equal or less then specified"
        def ts = response.then().spec(readSpec())
                .extract().path("data.findAll().attributes.timestamp")
        ts.each{ x ->
            assertThat(LocalDateTime.parse(x).toLocalDate(), lessThanOrEqualTo(LocalDate.parse("2017-02-04")))
        }
    }

    def "User requests transactions between specified dates"() {
        given: "Several transactions with different dates"
        TransactionFixture.createMultiple()

        when: "Transaction list filtered using notLater"
        def response = given()
                .queryParam("notEarlier", '2017-02-05T00:00:00')
                .queryParam("notLater", '2017-02-06T00:00:00')
                .when().get(API.Transactions)

        then: "Every timestamp in returned list should be equal to specified"
        def ts = response.then().spec(readSpec())
                .extract().path("data.findAll().attributes.timestamp")
        ts.each{ x ->
            assertThat(LocalDateTime.parse(x).toLocalDate(), is(LocalDate.parse("2017-02-05")))
        }
    }


    def "User sorts transaction on timestamp ascending"() {
        given: "Several transactions with different dates"
        TransactionFixture.createMultiple()

        when: "Transaction list sorted by timestamp requested"
        def response = given()
                .queryParam("sort", "-timestamp")
                .when().get(API.Transactions)

        then: "Every timestamp in returned list should be equal or less then previous"
        def ts = response.then().spec(readSpec())
                .extract().path("data.findAll().attributes.timestamp")
        def current = ts.first()
        ts.each{ x ->
            assertThat(x, greaterThanOrEqualTo(current))
            current = x
        }
    }

    def "User filters transaction by account"() {
        given: "Several accounts and transaction on them"
        def incomeId = AccountFixture.create(AccountFixture.incomeAccount())
        def assetId = AccountFixture.create(AccountFixture.assetAccount())
        def expenseId = AccountFixture.create(AccountFixture.expenseAccount())
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : '2017-02-05T16:45:36',
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": incomeId,
                                                "amount"    : -150
                                        ],
                                        [
                                                "account_id": assetId,
                                                "amount"    : 50
                                        ],
                                        [
                                                "account_id": expenseId,
                                                "amount"    : 100
                                        ]
                                ]
                        ]
                ]
        ]
        given().body(JsonOutput.toJson(transaction))
                .when().post(API.Transactions)
                .then().spec(createSpec("/api/transaction"))


        when: "Transaction is filtered by account_id"
        def response = given()
                .queryParam("filter", "{\"account_id\": [${assetId}]} ")
                .when().get(API.Transactions)

        then: "Should only return transactions, matching filter"
        response.then().spec(readSpec())
                .body("data.size()", is(1)) //As we use new accounts for each test, there should be only one matching transaction
    }
}
