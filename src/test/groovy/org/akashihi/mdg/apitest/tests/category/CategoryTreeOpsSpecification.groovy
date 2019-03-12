package org.akashihi.mdg.apitest.tests.category

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.CategoryFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.errorSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*

class CategoryTreeOpsSpecification extends Specification {
    def setupSpec() {
        setupAPI()
    }

    def makeTree() {
        def outerId = CategoryFixture.create()
        def middleId = CategoryFixture.create(CategoryFixture.category(outerId))
        def innerId = CategoryFixture.create(CategoryFixture.category(outerId))
        return [
                "outer": outerId,
                "middle": middleId,
                "inner": innerId
        ]
    }

    def 'Check that category with parent correctly parented'() {
        given: 'Top level category'
        def outerId = CategoryFixture.create()

        when: 'Children are added to that category'
        def middleCategory = CategoryFixture.category(outerId)
        def middleId = CategoryFixture.create(middleCategory)
        def innerCategory = CategoryFixture.category(middleId)
        def innerId = CategoryFixture.create(innerCategory)

        then: 'All kids are returned with the top level category'
        when().get(API.Category, outerId)
                .then().spec(readSpec())
                .body("data.id", is(outerId))
                .body("data.attributes.children[0].id", is(middleId))
                .body("data.attributes.children[0].children[0].id", is(innerId))
    }

    def 'Category parent can be changed'() {
        given: 'Tree of 3 categories'
        def ids = makeTree()

        when: 'Inner category is reparented to the middle'
        def innerCategory = CategoryFixture.category(ids['outer'])
        CategoryFixture.update(ids['inner'], innerCategory)

        then: 'Outer category have two kids'
        when().get(API.Category, ids['outer'])
                .then().spec(readSpec())
                .body("data.attributes.children.size()", is(2))
                .body("data.attributes.children[0].children", is(nullValue()))
                .body("data.attributes.children[1].children", is(nullValue()))
    }

    def 'Cyclic reparenting is prevented'() {
        given: 'Tree of 3 categories'
        def ids = makeTree()

        when: 'Outer category is reparented to the inner one'
        def outerCategory = CategoryFixture.category(ids['inner'])
        then: 'Cycle is prevented and error is returned'
        given().body(JsonOutput.toJson(outerCategory))
            .when().put(API.Category, ids['outer'])
            .then().spec(errorSpec(412, "CATEGORY_TREE_CYCLED"))
    }

    def 'Setting parent to self moves tree to the top'() {
        given: 'Tree of 3 categories'
        def ids = makeTree()

        when: 'Middle category is reparented to self'
        def middleCategory = CategoryFixture.category(ids['middle'])
        CategoryFixture.update(ids['middle'], middleCategory)

        then: 'Middle becames top level parent'
        when().get(API.Category, ids['outer']).
                then().spec(readSpec())
                .body("data.id", is(ids['outer']))
                .body("data.attributes.children[0].id", is(ids['inner']))
                .body("data.attributes.children[0].children", is(nullValue()))
    }

    def 'Category of one type can not be parented to category of different type'() {
        given: 'Tree of 3 categories'
        def ids = makeTree()

        when: "Another type category is parented to that tree"
        def otherCategory = CategoryFixture.category(ids['outer'])
        otherCategory.data.attributes.account_type = 'income'

        then: 'Creation is declined'
        given().body(JsonOutput.toJson(otherCategory))
                .when().post(API.Categories)
                .then()spec(errorSpec(412, "CATEGORY_INVALID_TYPE"))
    }

    def 'Category of one type can not be reparented to category of different type'() {
        given: 'Tree of 3 categories and another one category'
        def ids = makeTree()
        def otherCategory = CategoryFixture.category()
        otherCategory.data.attributes.account_type = 'income'
        def otherCategoryId = CategoryFixture.create(otherCategory)

        when: "New category of different type is created under that tree"
        def middleCategory = CategoryFixture.category(otherCategoryId)

        then: "Reparenting is prevented"
        given().body(JsonOutput.toJson(middleCategory))
                .when().put(API.Category, ids["middle"])
                .then().spec(errorSpec(412, "CATEGORY_INVALID_TYPE"))
    }
}