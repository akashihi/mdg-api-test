package org.akashihi.mdg.apitest.tests.tag

import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.CoreMatchers.hasItems

class TagSpecification extends Specification {
    def setupSpec() {
        setupAPI()
    }

    def 'When transaction with new tag is posted, tag should be in tags list'() {
       given: 'Transaction with some tags'
       TransactionFixture.create(TransactionFixture.incomeTransaction())

       when: 'Tag list is retrieved'
       def response = when().get(API.Tags)

       then: "It should contain transaction tags"
       response.then().spec(readSpec())
            .body("data.findAll().attributes.txtag", hasItems("income", "transaction"))
    }
}
