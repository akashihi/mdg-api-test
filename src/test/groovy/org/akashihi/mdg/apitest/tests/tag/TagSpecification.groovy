package org.akashihi.mdg.apitest.tests.tag

import com.jayway.jsonpath.JsonPath
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.CoreMatchers.hasItems
import static org.hamcrest.Matchers.arrayContainingInAnyOrder
import static org.junit.Assert.assertThat

class TagSpecification extends Specification {
    TransactionFixture f = new TransactionFixture();

    def setupSpec() {
        setupAPI();
    }

    def 'When transaction with new tag is posted, tag should be in tags list'() {
       given: 'Transaction with some tags'
       f.makeIncomeTransaction()

       when: 'Tag list is retrieved'
       def response = given()
               .contentType("application/vnd.mdg+json").
               when()
               .get("/tag")

       then: "It should contain transaction tags"
       def body = JsonPath.parse(response.then()
               .assertThat().statusCode(200)
               .assertThat().contentType("application/vnd.mdg+json")
               .extract().asString())
       def tags = body.read("data[*].attributes.txtag", List.class)
       assertThat(tags, hasItems("income", "transaction"))
    }
}
