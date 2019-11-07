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
 