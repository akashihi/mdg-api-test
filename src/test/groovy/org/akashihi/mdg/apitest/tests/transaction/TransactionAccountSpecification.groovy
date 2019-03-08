package org.akashihi.mdg.apitest.tests.transaction

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.equalTo

class TransactionAccountSpecification extends Specification {
    TransactionFixture f = new TransactionFixture();

    def setupSpec() {
        setupAPI()
    }

    def 'New transaction changes account balance'() {
        given: "Several accounts"
        def accounts = f.prepareAccounts()

        when: "New transaction on those accounts is submitted"
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
                                                "amount"    : 150
                                        ]
                                ]
                        ]
                ]
        ]
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post(API.Transactions).
                then()
                .assertThat().statusCode(201)

        then: "Balance on accounts is changed"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Account, accounts['income']).
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.attributes.balance", equalTo(-150))

        given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Account, accounts['asset']).
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.attributes.balance", equalTo(150))
    }

    def 'Chaning transaction ops is reflected on accounts'() {
        given: "New transaction is submitted"
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
                                                "amount"    : 150
                                        ]
                                ]
                        ]
                ]
        ]
        def txId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post(API.Transactions).
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().path("data.id")

        when: "Transaction is modified"
        transaction.data.attributes.operations[0].amount = -30
        transaction.data.attributes.operations[1].amount = 30
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .put(API.Transaction, txId)

        then: "Accounts balances should be changed"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Account, accounts['income']).
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.attributes.balance", equalTo(-30))

        given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Account, accounts['asset']).
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.attributes.balance", equalTo(30))
    }

    def 'Deleting transaction reverts changes on accounts'() {
        given: "New transaction is submitted"
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
                                                "amount"    : 150
                                        ]
                                ]
                        ]
                ]
        ]
        def txId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post(API.Transactions).
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().path("data.id")


        when: "Transaction is deleted"
        when().delete(API.Transaction, txId)
                .then().assertThat().statusCode(204)

        then: "Accounts should have zero balance"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Account, accounts['income']).
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.attributes.balance", equalTo(0))

        given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Account, accounts['asset']).
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.attributes.balance", equalTo(0))
    }

}
