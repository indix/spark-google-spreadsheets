package com.github.potix2.spark.google.spreadsheets.util

import java.io.InputStream
import java.security.PrivateKey

import com.google.api.client.util.SecurityUtils
import org.jets3t.service.impl.rest.httpclient.RestS3Service
import org.jets3t.service.security.AWSCredentials
import spray.json.DefaultJsonProtocol
import spray.json._

case class S3Config(accessKey: String, secretKeyId: String, bucketName: String, objectName: String)

object S3Config extends DefaultJsonProtocol {
  implicit val statsConfigJsonFormat = jsonFormat4(S3Config.apply)
  def apply(jsonConfig: String): S3Config = jsonConfig.parseJson.convertTo[S3Config]
}

object Credentials {
  def getPrivateKeyFromInputStream(fileInputStream: InputStream): PrivateKey = {
    SecurityUtils.loadPrivateKeyFromKeyStore(
      SecurityUtils.getPkcs12KeyStore(),
      fileInputStream,
      "notasecret",
      "privatekey",
      "notasecret")
  }

  def getCredentialsFromS3File(s3Config: S3Config): PrivateKey = {
    val creds2 = new AWSCredentials(s3Config.accessKey, s3Config.secretKeyId)
    val s3Service = new RestS3Service(creds2)
    val bucket = s3Service.createBucket(s3Config.bucketName)
    val stream = s3Service.getObject(bucket, s3Config.objectName).getDataInputStream()

    getPrivateKeyFromInputStream(stream)
  }
}
