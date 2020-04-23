package ca.bccpc.covid.routes

import ca.bccpc.covid.data.CriteriaData
import ca.bccpc.covid.data.CriteriaType
import ca.bccpc.covid.data.ResourceSheet
import io.javalin.Context

object CriteriaController {
    fun getAllCriteria(context: Context) {
        val optionsByType = CriteriaType.values()
                .filter { it.display }
                .associateBy (
                        { it.name.toLowerCase() },
                        { CriteriaData(it.column, ResourceSheet.findOptions(it)) }
                )

        context.json(optionsByType)
    }
}
