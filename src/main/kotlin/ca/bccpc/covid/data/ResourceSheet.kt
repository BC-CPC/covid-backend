package ca.bccpc.covid.data

import krangl.DataFrame
import krangl.DataFrameRow
import kotlin.properties.Delegates

object ResourceSheet {
    var source: DataFrame by Delegates.notNull()

    fun findOptions(criteriaType: CriteriaType): List<String> {
        return source.rows
                .map {
                    it.findValue(criteriaType)
                }
                .filter(String::isNotEmpty)
                .distinct()
    }

    private fun DataFrameRow.findValue(column: CriteriaType): String {
        return (this[column.column] as? String) ?: ""
    }

    fun findResources(query: Map<CriteriaType, String>): List<Resource> {
        val results = source.filterByRow {
            query.entries.all {
                it.value.isEmpty() ||
                        findValue(it.key).equals(it.value, ignoreCase = true)
            }
        }

        return results.rows.map {
            Resource (
                    it.findValue(CriteriaType.NAME),
                    it.findValue(CriteriaType.PUBLISHER),
                    it.findValue(CriteriaType.ORIGIN),
                    it.findValue(CriteriaType.DATE),
                    it.findValue(CriteriaType.RESOURCE_TYPE),
                    it.findValue(CriteriaType.LINK)
            )
        }.sortedBy { it.date }
    }
}
