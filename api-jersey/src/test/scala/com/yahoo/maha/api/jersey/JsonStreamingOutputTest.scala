package com.yahoo.maha.api.jersey

import java.io.OutputStream
import java.util.Date

import com.yahoo.maha.api.jersey.example.ExampleMahaService
import com.yahoo.maha.api.jersey.example.ExampleSchema.StudentSchema
import com.yahoo.maha.core.bucketing.{BucketParams, UserInfo}
import com.yahoo.maha.core.query._
import com.yahoo.maha.core.request.ReportingRequest
import com.yahoo.maha.core.{Engine, OracleEngine}
import com.yahoo.maha.service.MahaRequestContext
import com.yahoo.maha.service.utils.MahaRequestLogHelper
import org.scalatest.FunSuite

/**
 * Created by pranavbhole on 06/04/18.
 */
class JsonStreamingOutputTest extends FunSuite {

  val jsonRequest = s"""{
                          "cube": "student_performance",
                          "selectFields": [
                            {"field": "Student ID"},
                            {"field": "Class ID"},
                            {"field": "Section ID"},
                            {"field": "Total Marks"}
                          ],
                          "filterExpressions": [
                            {"field": "Day", "operator": "between", "from": "${ExampleMahaService.yesterday}", "to": "${ExampleMahaService.today}"},
                            {"field": "Student ID", "operator": "=", "value": "213"}
                          ]
                        }"""

  val reportingRequest = ReportingRequest.deserializeSync(jsonRequest.getBytes, StudentSchema).toOption.get

  val query  = {
    val mahaService = ExampleMahaService.getMahaService("test")
    val mahaServiceConfig = mahaService.getMahaServiceConfig
    val registry = mahaServiceConfig.registry.get(ExampleMahaService.REGISTRY_NAME).get

    val bucketParams = BucketParams(UserInfo("uid", isInternal = true))

    val mahaRequestContext = MahaRequestContext(ExampleMahaService.REGISTRY_NAME,
      bucketParams,
      reportingRequest,
      jsonRequest.getBytes,
      Map.empty, "rid", "uid")

    val requestModel = mahaService.generateRequestModel(ExampleMahaService.REGISTRY_NAME, reportingRequest, BucketParams(UserInfo("test", false)), MahaRequestLogHelper(mahaRequestContext, mahaService.mahaRequestLogWriter)).toOption.get
    val factory = registry.queryPipelineFactory.from(requestModel.model, QueryAttributes.empty)
    factory.get.queryChain.drivingQuery
  }

  val timeStampString = new Date().toString

  class StringStream extends OutputStream {
    val stringBuilder = new StringBuilder()
    override def write(b: Int): Unit = {
      stringBuilder.append(b.toChar)
    }
    override def toString() : String = stringBuilder.toString()
  }

  case class TestOracleIngestionTimeUpdater(engine: Engine, source: String) extends IngestionTimeUpdater {
    override def getIngestionTime(dataSource: String): Option[String] = {
      Some(timeStampString)
    }
  }

  test("Test JsonStreamingOutput") {

    /*val rowList = CompleteRowList(query)

    val row = rowList.newRow
    row.addValue("Student ID", 123)
    row.addValue("Class ID", 234)
    row.addValue("Section ID", 345)
    row.addValue("Total Marks", 99)
    rowList.addRow(row)

    val jsonStreamingOutput = JsonStreamingOutput(
      query.queryContext.requestModel.reportingRequest.,
      Map(OracleEngine-> TestOracleIngestionTimeUpdater(OracleEngine, "testSource")))

    val stringStream =  new StringStream()

    jsonStreamingOutput.write(stringStream)
    val result = stringStream.toString()
    println(result)
    stringStream.close()
    assert(result.equals(s"""{"header":{"lastIngestTime":"$timeStampString","source":"student_grade_sheet","cube":"student_performance","fields":[{"fieldName":"Student ID","fieldType":"DIM"},{"fieldName":"Class ID","fieldType":"DIM"},{"fieldName":"Section ID","fieldType":"DIM"},{"fieldName":"Total Marks","fieldType":"FACT"}],"maxRows":200},"rows":[[123,234,345,99]]}"""))*/
  }

}