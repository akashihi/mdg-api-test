package org.akashihi.mdg.apitest.tests.account

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.AccountFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.createSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class AccountPrimaryBalanceSpecification extends Specification {
    def account = [
            "data": [
                    "type"      : "expenseAccount",
                    "attributes": [
                            "account_type": "expense",
                            "currency_id" : 978,
                            "name"        : "Rent"
                    ]
            ]
    ]

    def incomeAccount = [
            "data": [
                    "type"      : "expenseAccount",
                    "attributes": [
                            "account_type": "income",
                            "currency_id" : 978,
                            "name"        : "Salary"
                    ]
            ]
    ]

    def setupSpec() {
        setupAPI()
    }

    def "User creates new account"() {
        given: "Account in a non-default currency"
        def accountId = AccountFixture.create()

        def incomeAccountId = AccountFixture.create(AccountFixture.incomeAccount())

        when: "New operation on that expenseAccount is made"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2017-02-04T16:45:36",
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": accountId,
                                                "amount"    : 1000
                                        ],
                                        [
                                                "account_id": incomeAccountId,
                                                "amount"    : -1000
                                        ]
                                ]
                        ]
                ]
        ]

        given().body(JsonOutput.toJson(transaction))
                .when().post(API.Transactions).
                then().spec(createSpec("/api/transaction"))

        then: "Account appears on the accounts list"
        when().get(API.Accounts)
                .then().spec(readSpec())
                .body("data.find {it.id == ${accountId}}.attributes.balance", is(1000))
                .body("data.find {it.id == ${accountId}}.attributes.primary_balance", is(1190))
    }

    def "User checks account data"() {
        given: "Account in a non-default currency"
        def accountId = AccountFixture.create()

        def incomeAccountId = AccountFixture.create(AccountFixture.incomeAccount())

        when: "New operation on that expenseAccount is made"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2017-02-04T16:45:36",
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": accountId,
                                                "amount"    : 1000
                                        ],
                                        [
                                                "account_id": incomeAccountId,
                                                "amount"    : -1000
                                        ]
                                ]
                        ]
                ]
        ]

        given().body(JsonOutput.toJson(transaction))
                .when().post(API.Transactions).
                then().spec(createSpec("/api/transaction"))

        then: "Account object should be returned"
        when().get(API.Account, accountId)
                .then().spec(readSpec())
                .body("data.attributes.balance", equalTo(1000))
                .body("data.attributes.primary_balance", equalTo(1190))
    }
}
