package org.akashihi.mdg.apitest.tests.transaction

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.createSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*

class TransactionMulticurrencySpeficifation extends Specification {
    def setupSpec() {
        setupAPI()
    }

    def "User creates new multicurrency transaction"() {
        when: "New multicurrency transaction on those accounts is submitted"
        def transaction = TransactionFixture.multiCurrencyTransaction()
        def txId = given().body(JsonOutput.toJson(transaction))
                .when().post(API.Transactions)
                .then().spec(createSpec("/api/transaction/"))
                .body("data.type", equalTo("transaction"))
                .body("data.attributes.comment", equalTo("Test transaction"))
                .body("data.attributes.tags", containsInAnyOrder("test", "transaction"))
                .body("data.attributes.operations.findAll().amount", containsInAnyOrder(-100, 200))
                .body("data.attributes.operations.findAll().rate", containsInAnyOrder(2, 1))
                .extract().path("data.id")

        then: "Transaction appears on transaction list"
        given().when().get(API.Transactions)
                .then().spec(readSpec())
                .body("data.find {it.id==${txId}}.attributes.operations.findAll().amount", containsInAnyOrder(-100, 200))
                .body("data.find {it.id==${txId}}.attributes.operations.findAll().rate", containsInAnyOrder(2, 1))
    }

}
