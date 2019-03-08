package org.akashihi.mdg.apitest.tests.transaction

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class TransactionMulticurrencySpeficifation extends Specification {
    TransactionFixture f = new TransactionFixture();

    def setupSpec() {
        setupAPI();
    }

    def "User creates new multicurrency transaction"() {
        given: "Several accounts"
        def accounts = f.prepareAccounts()

        when: "New multicurrency transaction on those accounts is submitted"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2017-02-05T13:54:35",
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": accounts["asset"],
                                                "rate": 2,
                                                "amount"    : -100
                                        ],
                                        [
                                                "account_id": accounts["usdAsset"],
                                                "amount"    : 200
                                        ]
                                ]

                        ]
                ]
        ]
        def response =given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post(API.Transactions)
        def body = JsonPath.parse(response.
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .assertThat().header("Location", containsString("/api/transaction/"))
                .extract().asString())

        assertThat(body.read("data.type"), equalTo("transaction"))
        assertThat(body.read("data.attributes.comment"), equalTo("Test transaction"))
        assertThat(body.read("data.attributes.tags"), containsInAnyOrder("test", "transaction"))
        assertThat(body.read("data.attributes.operations.*.amount"), containsInAnyOrder(-100, 200))
        assertThat(body.read("data.attributes.operations.*.rate"), containsInAnyOrder(2, 1))
        def txId =response.then().extract().path("data.id")

        then: "Transaction appears on transaction list"
        def listResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Transactions)
        def listBody =  JsonPath.parse(listResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        assertThat(listBody.read("data", List.class).size(), is(not(0)))
        assertThat(listBody.read("data[?(@.id == ${txId})].type", List.class).first(), equalTo("transaction"))
        assertThat(listBody.read("data[?(@.id == ${txId})].attributes.comment", List.class).first(), equalTo("Test transaction"))
        assertThat(listBody.read("data[?(@.id == ${txId})].attributes.tags", List.class).first(), containsInAnyOrder("test", "transaction"))
        assertThat(listBody.read("data[?(@.id == ${txId})].attributes.operations.*.amount"), containsInAnyOrder(-100, 200))
        assertThat(listBody.read("data[?(@.id == ${txId})].attributes.operations.*.rate"), containsInAnyOrder(2, 1))
    }

}
