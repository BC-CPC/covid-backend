package ca.bccpc.covid

import ca.bccpc.covid.data.CriteriaAdapter
import ca.bccpc.covid.data.CriteriaType
import ca.bccpc.covid.data.ResourceSheet
import ca.bccpc.covid.routes.CriteriaController
import ca.bccpc.covid.routes.SearchController
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.json.FromJsonMapper
import io.javalin.json.JavalinJson
import io.javalin.json.ToJsonMapper
import krangl.ColType
import krangl.DataFrame
import krangl.readCSV
import org.apache.commons.csv.CSVFormat
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.client.util.InputStreamResponseListener
import org.eclipse.jetty.http.HttpStatus
import org.eclipse.jetty.util.ssl.SslContextFactory
import java.io.File
import java.io.FileOutputStream
import java.lang.StringBuilder
import java.lang.reflect.Modifier
import java.nio.file.Files
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main() {
    // create our web server
    val app = Javalin.create()
            .port(System.getenv("SERVER_PORT")?.toIntOrNull() ?: 80)
            .enableCorsForAllOrigins()
            .start()

    // JSON deserializer (includes validation)
    val moshi = Moshi.Builder()
            .add(CriteriaType::class.java, CriteriaAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()

    // JSON serializer
    val gson: Gson = GsonBuilder()
            .disableHtmlEscaping()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT)
            .create()

    // set mappers to use our serializers/deserializers
    JavalinJson.toJsonMapper = object: ToJsonMapper {
        override fun map(obj: Any): String {
            return gson.toJson(obj)
        }
    }

    JavalinJson.fromJsonMapper = object: FromJsonMapper {
        override fun <T> map(json: String, targetClass: Class<T>): T {
            return moshi.adapter(targetClass).fromJson(json)!!
        }
    }

    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
        try {
            fetchResourceData()
        } catch (e: Exception) {
            println("Could not fetch resource data!")
            e.printStackTrace()
        }
    }, 1L, 1, TimeUnit.DAYS)

    fetchResourceData()

    app.routes {
        path("criteria") {
            get(CriteriaController::getAllCriteria)
        }

        path("search") {
            post(SearchController::search)
        }
    }
}

fun fetchResourceData() {
    val csvFile = File("resource-data.csv")
    val excelFile = File("resource-data.xlsx")

    csvFile.delete()

    convertToCsv(excelFile, csvFile)

    ResourceSheet.source = DataFrame.readCSV(
            csvFile,
            CSVFormat.DEFAULT.withHeader(),
            mapOf (
                    ".default" to ColType.String
            )
    )
}

fun convertToCsv(excelFile: File, csvFile: File) {
    csvFile.createNewFile()

    val output = FileOutputStream(csvFile).writer()
    val workbook = WorkbookFactory.create(excelFile)
    val sheet = workbook.getSheetAt(0)

    for (i in 0..sheet.lastRowNum) {
        val rowBuilder = StringBuilder()
        val row = sheet.getRow(i)

        for (j in 0 until row.lastCellNum) {
            val cell = row.getCell(j)

            if (cell == null) {
                rowBuilder.append(" ,")
                continue
            }

            rowBuilder.append(when (cell.cellType) {
                CellType.NUMERIC -> cell.numericCellValue.toInt()
                else -> cell.stringCellValue.trim().replace("\n", "")
            })
            rowBuilder.append(',')
        }

        output.write(rowBuilder.toString().dropLast(1))
        output.write("\n")
    }

    output.flush()
    output.close()
}
