## 5 November 2019

In many places we want to refer to some other value like e.g. `${aws_route53_zone.primary.zone_id}`. Here's the example 
in the context:

```
resource "aws_route53_record" "website_alias" {
  zone_id = "${aws_route53_zone.primary.zone_id}"
```

In scala DSL it can be expressed as:

```
val primaryZone = NamedResource("primary", AwsRoute53Zone(...)
val record = NamedResource("website_alias", AwsRoute53Record(zoneId = primaryZone.out.zoneId, ...)
```

There's a problem with generating terraform JSON/HCL for the above scala code though. The problem lies in 
lack of ability to generate `aws_route53_zone.primary.zone_id`, the `primary` part is a problem, other
parts are easily achievable. `primary` part is a problem because 
 
## 10 January 2020
How can I distinguish between arguments and attributes: 
https://discuss.hashicorp.com/t/providers-schema-how-to-distinguish-attributes-from-arguments/5029 

Script invocation:

```
codegen/reStart newSbtProject --sbt-org-name pl.msitko --sbt-project-name terraform4s-provider-aws --sbt-project-version 0.1.0 --provider-name aws --provider-version 2.43.0 --sbt-project-path aws-provider --out-package-name terraform4s
```

## 11 Feb 2020

It's valid:

```
resource "aws_s3_bucket" "example_bucket" {
  bucket = "${aws_kinesis_stream.example_stream.name}-bucket}"
  acl    = "simplyincorrect"

  tags = {
    Environment = "test"
  }
}
```

Mind an incorrect `acl` value. `terraform plan` passes.
