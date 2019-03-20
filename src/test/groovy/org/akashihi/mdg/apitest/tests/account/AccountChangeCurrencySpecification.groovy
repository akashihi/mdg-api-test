package org.akashihi.mdg.apitest.tests.account

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.API
import org.akashihi.mdg.apitest.fixtures.AccountFixture
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.modifySpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.equalTo

class AccountChangeCurrencySpecification extends Specification {
    static def eurAccount() {
        return [
                "data": [
                        "type"      : "account",
                        "attributes": [
                                "account_type": "income",
                                "currency_id" : 978,
                                "name"        : "EUR"
                        ]
                ]
        ]
    }

    static def usdAccount() {
        return [
                "data": [
                        "type"      : "account",
                        "attributes": [
                                "account_type": "income",
                                "currency_id" : 840,
                                "name"        : "USD"
                        ]
                ]
        ]
    }

    static def czkAccount() {
        return [
                "data": [
                        "type"      : "account",
                        "attributes": [
                                "account_type": "income",
                                "currency_id" : 203,
                                "name"        : "CZK"
                        ]
                ]
        ]
    }

    def setupSpec() {
        setupAPI()
    }

    def "Transaction with same currency rebalanced precisely"() {
        given: "We have EUR and USD account with transaction on them"
        def eurId = AccountFixture.create(eurAccount())
        def usdId = AccountFixture.create(usdAccount())

        def tx = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : '2017-02-06T16:45:36',
                                "comment"   : "Same currency",
                                "tags"      : ["spend"],
                                "operations": [
                                        [
                                                "account_id": eurId,
                                                "amount"    : -100,
                                                "rate"      : 1
                                        ],
                                        [
                                                "account_id": usdId,
                                                "amount"    : 80,
                                                "rate"      : 1.25
                                        ]
                                ]
                        ]
                ]
        ]
        def txId = TransactionFixture.create(tx)

        when: "USD account is changed to EUR account"
        def usd2eur = usdAccount()
        usd2eur.data.attributes.currency_id = 978
        given().body(JsonOutput.toJson(usd2eur))
                .when().put(API.Account, usdId)
                .then().spec(modifySpec())
                .body("data.attributes.currency_id", equalTo(978))

        then: "Balance is recalculated to new currency"
        when().get(API.Account, usdId).
                then().spec(readSpec())
                .body("data.attributes.balance", equalTo(100))

        when().get(API.Transaction, txId)
                .then().spec(readSpec())
                .body("data.attributes.operations.find {it.account_id==${usdId}}.rate", equalTo(1))
    }

    def "Transaction with default currency rebalanced correctly"() {
        given: "We have EUR and USD account with transaction on them"
        def eurId = AccountFixture.create(eurAccount())
        def czkId = AccountFixture.create(czkAccount())

        def tx = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : '2017-02-06T16:45:36',
                                "comment"   : "Default currency",
                                "tags"      : ["spend"],
                                "operations": [
                                        [
                                                "account_id": eurId,
                                                "amount"    : 100,
                                                "rate"      : 25
                                        ],
                                        [
                                                "account_id": czkId,
                                                "amount"    : -2500,
                                                "rate"      : 1
                                        ]
                                ]
                        ]
                ]
        ]
        def txId = TransactionFixture.create(tx)

        when: "EUR account is changed to CZK account"
        def eur2czk = czkAccount()
        eur2czk.data.attributes.currency_id = 203
        given().body(JsonOutput.toJson(eur2czk))
                .when().put(API.Account, eurId)
                .then().spec(modifySpec())
                .body("data.attributes.currency_id", equalTo(203))

        then: "Balance is recalculated to new currency"
        when().get(API.Account, eurId).
                then().spec(readSpec())
                .body("data.attributes.balance", equalTo(2500))

        when().get(API.Transaction, txId)
                .then().spec(readSpec())
                .body("data.attributes.operations.find {it.account_id==${eurId}}.rate", equalTo(1))
    }

    def "Transaction with several currencies have rate recalculated"() {
        given: "We have EUR and USD account with transaction on them"
        def eurId = AccountFixture.create(eurAccount())
        def czkId = AccountFixture.create(czkAccount())
        def usdId = AccountFixture.create(usdAccount())

        def tx = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : '2017-02-06T16:45:36',
                                "comment"   : "Multi currency",
                                "tags"      : ["spend"],
                                "operations": [
                                        [
                                                "account_id": eurId,
                                                "amount"    : 100,
                                                "rate"      : 25
                                        ],
                                        [
                                                "account_id": usdId,
                                                "amount"    : 100,
                                                "rate"      : 20
                                        ],
                                        [
                                                "account_id": czkId,
                                                "amount"    : -4500,
                                                "rate"      : 1
                                        ]
                                ]
                        ]
                ]
        ]
        def txId = TransactionFixture.create(tx)

        when: "CZK account is changed to USD account"
        def czk2usd = czkAccount()
        czk2usd.data.attributes.currency_id = 840
        given().body(JsonOutput.toJson(czk2usd))
                .when().put(API.Account, czkId)
                .then().spec(modifySpec())
                .body("data.attributes.currency_id", equalTo(840))

        then: "Balance is recalculated to new currency"
        when().get(API.Account, czkId).
                then().spec(readSpec())
                .body("data.attributes.balance", equalTo(-219))

        when().get(API.Account, eurId).
                then().spec(readSpec())
                .body("data.attributes.balance", equalTo(100))

        when().get(API.Account, usdId).
                then().spec(readSpec())
                .body("data.attributes.balance", equalTo(100))

        when().get(API.Transaction, txId)
                .then().spec(readSpec())
                .body("data.attributes.operations.find {it.account_id==${eurId}}.rate.toString()", equalTo("1.19"))
                .body("data.attributes.operations.find {it.account_id==${usdId}}.rate", equalTo(1))
                .body("data.attributes.operations.find {it.account_id==${czkId}}.rate", equalTo(1))
    }
}
