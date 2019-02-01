package org.akashihi.mdg.apitest.tests.category

import groovy.json.JsonOutput
import spock.lang.Specification

class CategoryAccountSpecification extends Specification {
    def outer = [
            "data": [
                    "type"      : "category",
                    "attributes": [
                            "name": "Bonuses",
                            "order" : 1,
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
                            "order" : 1,
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
                            "order" : 1,
                            "account_type" : "expense"
                    ]
            ]
    ]

    def makeTree() {
        def outerId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(category))
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
        given 'Top level category'
        def outerId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(category))
                .post("/category").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        when 'Children are added to that category'
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

        then 'All kids are returned with the top level category'
        def json = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/category/{id}", categoryId)
                .then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString()

        def tree = new JsonSlurper().parseTest(json)
        assertThat(tree.data.id, is(outerId))
        assertThat(tree.data.attributes.children[0].id, is(middleId))
        assertThat(tree.data.attributes.children[0].attributes.children[0].id, is(innerId))
    }

    def 'Category parent can be changed'() {
        given 'Tree of 3 categories'
        ids = makeTree()

        when 'Inner category is reparented to the middle'
        def middleCategory = middle.clone()
        middleCategory.data.attributes.parent_id = ids['outer']
        given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(middleCategory))
                .put("/category/{id}", categoryId).
                then()
                .assertThat().statusCode(202)
                .assertThat().contentType("application/vnd.mdg+json")

        then 'Outer category have two kids'
        def json = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/category/{id}", ids['outer']).
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString()
        def tree = new JsonSlurper().parseTest(json)
        def kids = tree.data.attributes.children

        assertThat(kids.size(), is(2))
        assertThat(kids[0].attributes.children, empty)
        assertThat(kids[1].attributes.children, empty)
    }

    def 'Cyclic reparenting is prevented'() {
        given 'Tree of 3 categories'
        ids = makeTree()

        when 'Outer category is reparented to the inner one'
        def outerCategory = outer.clone()
        outerCategory.data.attributes.parent_id = ids['inner']
        given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(outerCategory))
                .put("/category/{id}", ids['outer']).
                then()
                .assertThat().statusCode(202)
                .assertThat().contentType("application/vnd.mdg+json")

        then 'Middle becames outer'
        def json = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/category/{id}", ids['middle']).
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString()
        def tree = new JsonSlurper().parseTest(json)

        assertThat(tree.data.id, is(ids['middle']))
        assertThat(tree.data.attributes.children[0].id, is(ids['inner']))
        assertThat(tree.data.attributes.children[0].attributes.children[0].id, is(ids['outer']))
    }

    def 'No parent moves tree to the top'() {
        given 'Tree of 3 categories'
        ids = makeTree()

        when 'Outer category is reparented to the inner one'
        def middleCategory = middle.clone()
        outerCategory.data.attributes.parent_id = null
        given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(middleCategory))
                .put("/category/{id}", ids['middle']).
                then()
                .assertThat().statusCode(202)
                .assertThat().contentType("application/vnd.mdg+json")

        then 'Middle becames top level parent'
        def json = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/category/{id}", ids['middle']).
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString()
        def tree = new JsonSlurper().parseTest(json)

        assertThat(tree.data.id, is(ids['middle']))
        assertThat(tree.data.attributes.children[0].id, is(ids['inner']))
        assertThat(tree.data.attributes.children[0].attributes.children, empty())
    }

    def 'Category of one type can not be reparented to category of different type'() {
        given 'Tree of 3 categories and another one category'
        ids = makeTree()
        def otherCategory = middle.clone()
        otherCategory.data.attributes.account_type = 'income'
        given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(middleCategory))
                .put("/category/{id}", ids['middle']).
                then()
                .assertThat().statusCode(202)
                .assertThat().contentType("application/vnd.mdg+json")
        def otherId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(category))
                .post("/category").
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")
        otherCategory.data.id=otherId

        when 'Other category is reparented to the tree'
        otherCategory.data.attributes.parent_id = ids['outer']
        def response = given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(otherCategory))
                .put("/category/{id}", ids['otherId'])

        then 'Reparenting is declined'
        response.then()
                .assertThat().statusCode(412)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("errors[0].code", equalTo("CATEGORY_INVALID_TYPE"))
    }
}