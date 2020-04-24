package ca.bccpc.covid.routes

import ca.bccpc.covid.data.CriteriaType
import ca.bccpc.covid.data.Resource
import ca.bccpc.covid.data.ResourceSheet
import io.javalin.Context

object SearchController {
    fun search(context: Context) {
        val request = context.bodyAsClass(SearchRequest::class.java)

        context.json(ResourceSheet.findResources(request.query).distinctBy(Resource::name))
    }
}

class SearchRequest (
        val query: Map<CriteriaType, String>
)
