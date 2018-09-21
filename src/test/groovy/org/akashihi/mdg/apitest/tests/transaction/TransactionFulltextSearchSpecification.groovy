package org.akashihi.mdg.apitest.tests.transaction

import com.jayway.jsonpath.JsonPath
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.empty
import static org.hamcrest.Matchers.not
import static org.junit.Assert.assertThat

class TransactionFulltextSearchSpecification extends Specification {
    TransactionFixture f = new TransactionFixture()

    def setupSpec() {
        setupAPI()
        // Force full text search reindex
        when()
                .put("/setting/mnt.transaction.reindex")
                .then()
                .assertThat().statusCode(202)
    }

    def "User filters transaction by comment"() {
        given: "Several transactions with different comments"
        f.makeTransactions()

        when: "Filter on malformed comment is requested"
        def response = given()
                .queryParam("filter", "{\"comment\": \"incme\"} ")
                .contentType("application/vnd.mdg+json").
                when()
                .get("/transaction")

        then: "Should return transactions, matching filter"
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        assertThat(body.read("data[?(@.attributes.comment == 'Income transaction')]", List.class), not(empty()))
        assertThat(body.read("data[?(@.attributes.comment != 'Income transaction')]", List.class), empty())
    }
}
