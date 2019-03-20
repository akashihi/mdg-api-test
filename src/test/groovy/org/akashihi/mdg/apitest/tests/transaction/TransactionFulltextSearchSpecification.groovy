package org.akashihi.mdg.apitest.tests.transaction

import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.empty
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.nullValue

class TransactionFulltextSearchSpecification extends Specification {
    def setupSpec() {
        setupAPI()
        // Force full text search reindex
        when().put(API.Setting,"mnt.transaction.reindex")
                .then().statusCode(202)
    }

    def "User filters transaction by comment"() {
        given: "Several transactions with different comments"
        TransactionFixture.createMultiple()

        when: "Filter on malformed comment is requested"
        def response = given()
                .queryParam("filter", "{\"comment\": \"incme\"} ")
                .when().get(API.Transactions)

        then: "Should return transactions, matching filter"
        response.then().spec(readSpec())
                .body("data.find {it.attributes.comment=='Income transaction'}", not(empty()))
                .body("data.find {it.attributes.comment!='Income transaction'}", nullValue())
    }

    def "User filters transaction by tag"() {
        given: "Several transactions with different tags"
        TransactionFixture.createMultiple()


        when: "Transaction list sorted by timestamp requested"
        def response = given()
                .queryParam("filter", "{\"tag\": [\"trnsaction\"]} ")
                .when().get(API.Transactions)

        then: "Should only return transactions, matching filter"
        response.then().spec(readSpec())
                .body("data.find {it.attributes.comment=='Income transaction'}", not(empty()))
                .body("data.find {it.attributes.comment=='Test transaction'}", not(empty()))
                .body("data.find {it.attributes.comment=='Spend transaction'}", nullValue())
    }

}
