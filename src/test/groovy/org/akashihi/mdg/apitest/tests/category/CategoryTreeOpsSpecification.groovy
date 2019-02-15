package org.akashihi.mdg.apitest.tests.category

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertThat

class CategoryTreeOpsSpecification extends Specification {
    def outer = [
            "data": [
                    "type"      : "category",
                    "attributes": [
                            "name": "Bonuses",
                            "priority" : 1,
                            "account_type" : "expense"
                    ]
            ]
    ]

    def middle = [
            "data": [
                    "type"      : "category",
                    "attributes": [
                            "parent_id": -1,
                            "name": "Bonuses",
                            "priority" : 1,
                            "account_type" : "expense"
                    ]
            ]
    ]

    def inner = [
            "data": [
                    "type"      : "category",
                    "attributes": [
                            "parent_id": -1,
                            "name": "Bonuses",
                            "priority" : 1,
                            "account_type" : "expense"
                    ]
            ]
    ]

    def setupSpec() {
        setupAPI()
    }

    def makeTree() {
        def outerId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(outer))
                .post("/category").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")
        def middleCategory = middle.clone()
        middleCategory.data.attributes.parent_id = outerId
        def middleId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(middleCategory))
                .post("/category").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")
        def innerCategory = inner.clone()
        innerCategory.data.attributes.parent_id = middleId
        def innerId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(innerCategory))
                .post("/category").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")
        return [
                "outer": outerId,
                "middle": middleId,
                "inner": innerId
        ]
    }

    def 'Check that category with parent correctly parented'() {
        given: 'Top level category'
        def outerId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .log().all()
                .request().body(JsonOutput.toJson(outer))
                .post("/category").
                then()
                .log().all()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        when: 'Children are added to that category'
        def middleCategory = middle.clone()
        middleCategory.data.attributes.parent_id = outerId
        def middleId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(middleCategory))
                .post("/category").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")
        def innerCategory = inner.clone()
        innerCategory.data.attributes.parent_id = middleId
        def innerId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(innerCategory))
                .post("/category").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        then: 'All kids are returned with the top level category'
        def json = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/category/{id}", outerId)
                .then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString()

        def tree = new JsonSlurper().parseText(json)
        assertThat(tree.data.id, is(outerId))
        assertThat(tree.data.attributes.children[0].id, is(middleId))
        assertThat(tree.data.attributes.children[0].children[0].id, is(innerId))
    }

    def 'Category parent can be changed'() {
        given: 'Tree of 3 categories'
        def ids = makeTree()

        when: 'Inner category is reparented to the middle'
        def innerCategory = inner.clone()
        innerCategory.data.attributes.parent_id = ids['outer']
        given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(innerCategory))
                .put("/category/{id}", ids['inner']).
                then()
                .assertThat().statusCode(202)
                .assertThat().contentType("application/vnd.mdg+json")

        then: 'Outer category have two kids'
        def json = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/category/{id}", ids['outer']).
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString()
        def tree = new JsonSlurper().parseText(json)
        def kids = tree.data.attributes.children

        assertThat(kids.size(), is(2))
        assertNull(kids[0].children)
        assertNull(kids[1].children)
    }

    def 'Cyclic reparenting is prevented'() {
        given: 'Tree of 3 categories'
        def ids = makeTree()

        when: 'Outer category is reparented to the inner one'
        def outerCategory = outer.clone()
        outerCategory.data.attributes.parent_id = ids['inner']
        def response = given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(outerCategory))
                .put("/category/{id}", ids['outer'])
        then: 'Cycle is prevented and error is returned'
        response.then()
                .assertThat().statusCode(412)
                .body("errors[0].code", equalTo("CATEGORY_TREE_CYCLED"))
    }

    def 'Setting parent to self moves tree to the top'() {
        given: 'Tree of 3 categories'
        def ids = makeTree()

        when: 'Outer category is reparented to the inner one'
        def middleCategory = middle.clone()
        middleCategory.data.attributes.parent_id = ids['middle']
        given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(middleCategory))
                .put("/category/{id}", ids['middle']).
                then()
                .assertThat().statusCode(202)
                .assertThat().contentType("application/vnd.mdg+json")

        then: 'Middle becames top level parent'
        def json = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/category/{id}", ids['middle']).
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString()
        def tree = new JsonSlurper().parseText(json)

        assertThat(tree.data.id, is(ids['middle']))
        assertThat(tree.data.attributes.children[0].id, is(ids['inner']))
        assertNull(tree.data.attributes.children[0].children)
    }

    def 'Category of one type can not be parented to category of different type'() {
        given: 'Tree of 3 categories'
        def ids = makeTree()
        def otherCategory = middle.clone()
        otherCategory.data.attributes.account_type = 'income'

        when: "Another type category is parented to that tree"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(otherCategory))
                .post("/category")

        then: 'Reparenting is declined'
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("CATEGORY_INVALID_TYPE"))
    }

    def 'Category of one type can not be reparented to category of different type'() {
        given: 'Tree of 3 categories and another one category'
        def ids = makeTree()

        when: "New category of different type is created under that tree"

        def otherCategory = middle.clone()
        otherCategory.data.attributes.account_type = 'income'
        otherCategory.data.attributes.parent_id = ids["outer"]
        def response = given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(otherCategory))
                .post("/category")
        then: "Creation should be declined"
                response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("CATEGORY_INVALID_TYPE"))
    }
}