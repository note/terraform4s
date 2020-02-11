/**
  * This file has been generated by terraform4s at 2020-01-15T00:00:00Z
  * Terraform version: 0.12.19
  * Terraform provider name: aws
  * Terraform provider version: 2.43.0
  */
package pl.msitko.example.aws

import pl.msitko.terraform4s._

final case class AwsAcmCertificateValidationOut(
    certificateArn: Val[String],
    id: Val[String],
    validationRecordFqdns: Val[Option[Set[String]]])

final case class AwsAcmCertificateValidation(
    certificateArn: Val[String],
    id: Option[Val[String]],
    validationRecordFqdns: Option[Val[Set[String]]])(implicit r: ProvidersRoot)
    extends Resource[AwsAcmCertificateValidationOut](r) {

  override def out =
    AwsAcmCertificateValidationOut(
      OutStringVal(schemaName, resourceName, "certificateArn"),
      OutStringVal(schemaName, resourceName, "id"),
      OutVal[Option[Set[String]]](schemaName, resourceName, "validationRecordFqdns"))
  override def fields: List[Field] = List(Field("certificateArn", certificateArn))

  override def optionalFields: List[Option[Field]] =
    List(id.map(i => Field("id", i)), validationRecordFqdns.map(i => Field("validationRecordFqdns", i)))
  override def schemaName: String = "AwsAcmCertificateValidation"
}
