package com.github.potix2.spark.google.spreadsheets.util

import java.io.InputStream
import java.security.PrivateKey

import com.amazonaws.auth._
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.google.api.client.util.SecurityUtils

object Credentials {
  def getPrivateKeyFromInputStream(fileInputStream: InputStream): PrivateKey = {
    SecurityUtils.loadPrivateKeyFromKeyStore(
      SecurityUtils.getPkcs12KeyStore(),
      fileInputStream,
      "notasecret",
      "privatekey",
      "notasecret")
  }

  def getCredentialsFromS3File(bucketName: String, objectName: String, awsUseIAM: Boolean = true): PrivateKey = {
    val clientBuilder = AmazonS3ClientBuilder.standard()
    if (awsUseIAM) {
      clientBuilder.withCredentials(new InstanceProfileCredentialsProvider(true))
    }
    val client = clientBuilder.build()

    val stream = client.getObject(bucketName, objectName).getObjectContent
    getPrivateKeyFromInputStream(stream)
  }
}
