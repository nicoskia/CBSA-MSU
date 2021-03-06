package csst15

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.lang3.StringUtils

@Secured(['IS_AUTHENTICATED_FULLY'])
class AutoCompleteController {
    def autoCompleteService

    static allowedMethods = [
            loadEntity: 'POST'
    ]

    def loadInterests() {
        render autoCompleteService.loadEntity(params)
    }

    def loadEntity() {
        def entity = Entity.findByName(params.name)

        entity = entity.collect {
            [
                    type: entity.type.name,
                    name: entity.name,
                    desc: entity.description
            ]
        }
        render(entity as JSON)
    }

    def loadAuthors() {
        render autoCompleteService.loadAuthors(params)
    }

    def loadAuthorRefs() {
        def authorName = StringUtils.split(params.name, ',')
        def author = Author.findByFirstNameAndLastName(authorName[1].trim(), authorName[0].trim())
        def references = ReferenceAuthor.findAllByAuthor(author).reference.unique()
        def entity = Entity.findById(params.entity)
        def selectedReferences = ReferenceVote.findAllByEntityAndReferenceIsNotNull(entity, [cache: true])?.reference?.unique()

        def filteredReferences = references.findAll { reference ->
            !selectedReferences.id.contains(reference.id)
        }

        filteredReferences = filteredReferences.collect { reference ->
            [
                    citation: reference.citation,
                    id      : reference.id
            ]
        }

        render(filteredReferences as JSON)
    }

    def loadRefAuthorDetails() {
        if (params.id) {
            def reference = Reference.findById(params.id)
            def refAuthor = ReferenceAuthor.findAllByReference(reference)
            def results = refAuthor.collect { result ->
                [
                        id     : result.reference.id,
                        citation: result.reference.citation,
                        year   : result.reference.year,
                        authors:
                                refAuthor.author.collect { a ->
                                    a.lastName + "," + a.firstName
                                }

                ]
            }

            render(results as JSON)
        } else {
            redirect(uri: '/not-found')
        }
    }
}
